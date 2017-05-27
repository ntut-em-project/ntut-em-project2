"use strict";
//Dom
let AllComponent = [];
(function () {
    const CanvasContainer = $("#mainCanvas");

    class Component {
        constructor(type, deg) {
            let ele = this.ele = new Image();
            ele.id = '_u_' + new Date().getTime();
            this.deg = deg;
            this.type = type;

            ele.classList.add('component');

            this._onUpdateTypeDeg();
            this.attach();

            ele.component = this;
            AllComponent.push(this);
        }

        setDeg(deg) {
            this.deg = deg;
            this._onUpdateTypeDeg();
        }

        setType(type) {
            this.type = type;
            this._onUpdateTypeDeg();
        }

        showInfoPanel(){

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
        }

        set x(v) {
            this.ele.style.left = v + 'px';
        }

        set y(v) {
            this.ele.style.top = v + 'px';
        }
    }

    function drag(ev) {
        this.classList.add('dragging');

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
    }

    function dragEnd(ev) {
        this.classList.remove('dragging');
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

    biCom.find('*').prop('draggable', false);

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


