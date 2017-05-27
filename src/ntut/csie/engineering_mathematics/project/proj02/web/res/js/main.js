"use strict";
//Dom
let AllComponent = [], AllConnection = [], AllNodeCollection = [];
(function () {
    const CanvasContainer = $("#mainCanvas");
    const dialog = $('#myModal');
    const cs = document.getElementById('canvas');
    const ctx = cs.getContext('2d');

    let ClickedNode = [];
    let CurDragObj = null;
    const COLORS = [
        "#cd8700",
        "#cd400c",
        "#a0cc0a",
        "#8666cc",
        "#5400AD",
        "#2B25C3",
        "#25BCC3"
    ];
    const UNIT = {
        'R': 'kΩ',
        'L': 'mL',
        'C': 'µC',
        'E': 'V'
    };
    const INIT_V = {
        'L': 'i<sub>l</sub>(t=0)',
        'C': 'v<sub>c</sub>(t=0)',
    };
    const HaveInfo = (x) => {
        return "RLC".indexOf(x) >= 0;
    };

    const drawNode = (node) => {
        if (!node) return;
        ctx.fillStyle = "#000";

        ctx.beginPath();
        ctx.moveTo(node.x, node.y);
        ctx.arc(node.x, node.y, 5, 0, 2 * Math.PI);
        ctx.closePath();
        ctx.fill();
    };

    class Connection {
        constructor(n1, n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
    }

    class NodeCollection {
        constructor() {
            this.nodes = [];
            this.color = COLORS[AllNodeCollection.length % COLORS.length];
            AllNodeCollection.push(this);
        }

        add(node) {
            if (!this.nodes.some(n => node.c === n.c && node.id === n.id)) {
                this.nodes.push(node);
            }
        }

        static merge(nC1, nC2) {
            if (nC1 === nC2) return;
            nC1.nodes = nC1.nodes.concat(nC2.nodes);
            nC2.nodes.forEach(n => {
                n.c[`n${n.id}C`] = nC1;
            });
            let i = AllNodeCollection.indexOf(nC2);
            if (i >= 0) {
                AllNodeCollection.splice(i, 1);
            }
        }
    }

    class Component {
        constructor(type, deg) {
            let ele = this.ele = new Image();
            ele.draggable = true;
            ele.id = '_u_' + new Date().getTime();
            this.deg = deg;
            this.type = type;
            this.n1C = undefined;
            this.n2C = undefined;
            this.value = 1;
            this.initValue = 0;


            ele.classList.add('component');

            this._onUpdateTypeDeg();
            this.attach();

            ele.component = this;
            AllComponent.push(this);
            this.idx = AllComponent.filter(c => c.type === type).length;
        }

        setDeg(deg) {
            this.deg = deg;
            this._onUpdateTypeDeg();
        }

        setType(type) {
            this.type = type;
            this._onUpdateTypeDeg();
        }

        showInfoPanel() {
            if (!HaveInfo(this.type)) return;
            console.log('show info panel');
            dialog.data('c', this);
            dialog.modal('show');

            dialog.find('#m-bdy').html(`
            <form class="form-inline">
<div class="form-group">
    <label>數值</label>
    <div>
        <input value="${this.value}" type="number" class="form-control" id="f-v"/>
        <span>${UNIT[this.type]}</span>    
    </div>
</div>
<hr/>
<div class="form-group" ${INIT_V[this.type] ? '' : 'hidden'}>
    <label>初始值</label>
    <div>
        <span>${INIT_V[this.type]}</span>
        <input value="${this.initValue}" type="number" class="form-control" id="f-iv"/>
    </div>
    
</div>
            </form>
            `);
        }

        _onUpdateTypeDeg() {
            let src = 'images/';
            this.ele.setAttribute('data-type', this.type);
            this.ele.setAttribute('data-deg', this.deg);
            src += this.type.toLowerCase() + '-';
            src += this.deg.toString();
            src += '.png';

            this.ele.src = src;
        }

        attach() {
            CanvasContainer.append(this.ele);
            this.ele.addEventListener('dragstart', drag);
            this.ele.addEventListener('dragend', dragEnd);

            const t = this;
            const _X = .15;
            this.ele.addEventListener('dblclick', (e) => {
                this.showInfoPanel();
            });
            this.ele.addEventListener('click', (e) => {
                const b = this.ele.getBoundingClientRect();
                let node = undefined;
                let p = -1;

                if (t.deg == 1) {
                    p = e.layerY / b.height;
                } else {
                    p = e.layerX / b.width;
                }
                if (p > 1 - _X) {
                    node = t.Node2;
                } else if (p < _X) {
                    node = t.Node1;
                }

                console.log(p, node);

                if (!node) {
                    return;
                }

                const FIRST = ClickedNode[0];
                let same = false;
                if (FIRST) {
                    if (FIRST.c === node.c) same = true;
                }

                if (same) {
                    ClickedNode.shift();
                } else {
                    ClickedNode.unshift(node);
                }

                if (ClickedNode.length >= 2) {
                    AllConnection.push(new Connection(ClickedNode[0], ClickedNode[1]));
                    let k1 = `n${ClickedNode[0].id}C`;
                    let k2 = `n${ClickedNode[1].id}C`;

                    let nc = ClickedNode[0].c[k1];
                    let nc2 = ClickedNode[1].c[k2];

                    if (nc && nc2) {
                        NodeCollection.merge(nc, nc2);
                    } else if (!nc && nc2) {
                        nc = nc2;
                    } else if (nc && !nc2) {

                    } else if (!nc && !nc2) {
                        nc = new NodeCollection();
                    }

                    ClickedNode[0].c[k1] = ClickedNode[1].c[k2] = nc;

                    nc.add(ClickedNode[0]);
                    nc.add(ClickedNode[1]);

                    ClickedNode.length = 0;
                }
            });
        }

        get x() {
            return parseInt(this.ele.style.left, 10) || 0;
        }

        get y() {
            return parseInt(this.ele.style.top, 10) || 0;
        }

        set x(v) {
            this.ele.style.left = v + 'px';
        }

        set y(v) {
            this.ele.style.top = v + 'px';
        }

        get Node1() {
            const b = this.ele.getBoundingClientRect();
            const offset = (b.width - this.ele.width) / 2;
            let ret = {
                id: 1,
                c: this,
            };

            Object.defineProperty(ret, 'x', {
                get: () => {
                    return this.deg == 1 ? (this.x + b.width / 2) : (this.x + offset);
                },
            });
            Object.defineProperty(ret, 'y', {
                get: () => {
                    return this.deg == 1 ? (this.y + offset) : (this.y + b.height / 2);
                },
            });

            Object.defineProperty(ret, 'nc', {
                get: () => this.n1C,
                set: (v) => {
                    this.n1C = v;
                },
            });

            return ret;
        }

        get Node2() {
            const b = this.ele.getBoundingClientRect();
            const offset = (b.width - this.ele.width) / 2;
            let ret = {
                id: 2,
                c: this,
            };
            Object.defineProperty(ret, 'x', {
                get: () => {
                    return this.deg == 1 ? (this.x + b.width / 2) : (this.x + b.width - offset);
                },
            });

            Object.defineProperty(ret, 'y', {
                get: () => {
                    return this.deg == 1 ? (this.y + b.height - offset) : (this.y + b.height / 2);
                },
            });

            Object.defineProperty(ret, 'nc', {
                get: () => this.n2C,
                set: (v) => {
                    this.n2C = v;
                },
            });

            return ret;
        }

        draw() {
            const n1 = this.Node1, n2 = this.Node2;
            drawNode(n1);
            drawNode(n2);
            ctx.font = "20px Arial";
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            let text = this.type;
            let p = {};
            const OFFSET = 25;

            if (this.deg == 1) {
                p.x = n1.x + OFFSET;
                p.y = (n1.y + n2.y) / 2;
            } else {
                p.x = (n1.x + n2.x) / 2;
                p.y = n1.y - OFFSET;
            }

            if (HaveInfo(this.type)) {
                text += this.idx;
            } else {
                p.x -= OFFSET * 3;
            }

            ctx.fillText(text, p.x, p.y);


        }
    }

    function drag(ev) {
        this.classList.add('dragging');
        CanvasContainer.addClass('c-d');

        console.log(ev, ev.target);
        const refImg = ev.target;
        let dragImg = new Image();
        dragImg.src = refImg.src;

        let b = refImg.getBoundingClientRect();
        console.log(b);

        if (!refImg.id && refImg.component) {
        }

        let data = {
            type: refImg.getAttribute('data-type'),
            deg: refImg.getAttribute('data-deg'),
            size: {
                width: b.width,
                height: b.height,
            },
            id: refImg.id,
        };

        ev.dataTransfer.setData("text", JSON.stringify(data));
        ev.dataTransfer.setDragImage(dragImg, refImg.width / 2, refImg.height / 2);
        CurDragObj = this;
    }

    function dragEnd(ev) {
        this.classList.remove('dragging');
        CanvasContainer.removeClass('c-d');
        CurDragObj = null;
    }

    function drop(ev) {
        ev.preventDefault();
        console.log(ev);
        let data = JSON.parse(ev.dataTransfer.getData("text"));

        let c = data.id ? document.getElementById(data.id).component : new Component(data.type, data.deg);
        c.x = ev.layerX - data.size.width / 2;
        c.y = ev.layerY - data.size.height / 2;
    }

    function allowDrop(ev) {
        ev.preventDefault();
        ev.dataTransfer.dropEffect = "move";
    }

    function BCN(t, func, event) {
        func.call(t, event.originalEvent);
    }

    CanvasContainer.bind('drop', function (e) {
        BCN(this, drop, e);
    }).bind('dragover', function (e) {
        BCN(this, allowDrop, e);
    });

    let biCom = $(".component");
    biCom
        .prop('draggable', true)
        .bind('dragstart', function (e) {
            BCN(this, drag, e);
        })
        .bind('dragend', function (e) {
            BCN(this, dragEnd, e);
        });

    dialog.on('hidden.bs.modal', (e) => {
        const c = dialog.data('c');

        if (c) {
            let v = $("#f-v").val() * 1;
            let iv = $("#f-iv").val() * 1;
            c.value = v;
            c.initValue = iv;
        }

        dialog.data('c', null);
    });

    dialog.find('#rot_c').click(() => {
        const c = dialog.data('c');
        c.setDeg(1 - c.deg);
    });

    function drawAllC() {
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

        AllComponent.forEach(c => {
            c.draw();
        });

        AllConnection.forEach(c => {
            c.n1 = c.n1.c[`Node${c.n1.id}`];
            c.n2 = c.n2.c[`Node${c.n2.id}`];
            ctx.strokeStyle = c.n1.nc.color;
            ctx.lineWidth = 2;

            ctx.beginPath();
            ctx.moveTo(c.n1.x, c.n1.y);
            ctx.lineTo(c.n2.x, c.n2.y);
            ctx.stroke();
        });
        requestAnimationFrame(drawAllC);
    }

    cs.width = cs.offsetWidth;
    cs.height = cs.offsetHeight;
    drawAllC();
    {
        let E = new Component('E', '1');
        E.y = 400 / 2 - 128 / 2;
        E.x = 50;
    }
})();


function SolveODE(p, q, r, f, y0, yd0) {
    return new Promise((a, b) => {
        $.ajax("/SolODE", {
            method: "POST",
            data: {
                p: p,
                q: q,
                r: r,
                f: f,
                y0: y0,
                yd0: yd0,
            },
            dataType: "json",
            success: function (r) {
                console.log("ODE", r);
                a(r);
            },
            error: function (e) {
                b(e);
            }
        });
    });
}

function GetFunctionPoints(func, start, end, step) {
    return new Promise((a, b) => {
        $.ajax("/GetPts", {
            method: "POST",
            data: {
                func: func,
                start: start,
                end: end,
                step: step,
            },
            dataType: "json",
            success: function (r) {
                console.log("Points for eq: ", func, "From", start, "To", end);
                console.log(r);
                a(r);
            },
            error: function (e) {
                b(e);
            }
        });
    });
}

function TestSolODE() {
    SolveODE(1, -3, 2, 0, -2, 3);
    SolveODE(1, 0, -4, "-7*exp(2*t) + t", 1, 3);
    SolveODE(1, 0, -1, "5*sin(t)^2", 2, -4);
    SolveODE(0, 1, "1/(t-2)", "3*t", "(3)=4", null);
}

function TestGetPoints() {
    GetFunctionPoints("t^2", -5, 5, .5);
    GetFunctionPoints("exp(-t^2)", -5, 5, .5);
}


