"use strict";
//(function () {
const TYPES = [
    {
        name: 'RLC_S',
        kind: 'S',
        id: 1,
        conds: {
            R: 1,
            L: 1,
            C: 1,
            E: 1,
            node: 4
        },
    },
    {
        name: 'RLC_P',
        kind: 'P',
        id: 2,
        conds: {
            R: 1,
            L: 1,
            C: 1,
            I: 1,
            node: 2,
        }
    },
    {
        name: 'LC',
        kind: 'SP',
        id: 3,
        conds: {
            L: 1,
            C: 1,
            node: 2,
        }
    },
];

function getCurConds() {
    let ret = {
        R: 0,
        L: 0,
        C: 0,
        E: 0,
        I: 0,
        node: 0
    };

    AllComponent.forEach(c => {
        if (ret[c.type] !== undefined) ret[c.type]++;
    });

    ret.node = AllNodeCollection.length;

    return ret;
}

function getFirstComponentByType(type) {
    return AllComponent.filter(x => x.type === type)[0];
}

function getCurType() {
    const conds = getCurConds();
    for (let i = 0; i < TYPES.length; i++) {
        const T = TYPES[i];
        let same = true;
        for (let k in T.conds) {
            if (!T.conds.hasOwnProperty(k)) continue;
            if (T.conds[k] !== conds[k]) {
                same = false;
                break;
            }
        }
        if (same) return T;
    }

    return null;
}

function getPowerEQ() {
    let info = {
        type: $("#eType").val(),
        kind: $("#eKind").val(),
        f: $("#f").val(),
        pp: $("#pp").val(),
        dc: $("#dc").val(),
    };

    return ajax("/getPowerEQ", {
        data: info,
        type: "POST",
        dataType: "json"
    });
}

let _lastG = [];
function run() {
    const EQ = $("#eq").empty();
    const addEq = (vars, func) => {
        EQ.append(`
        <div class="eq">
            <var>${vars} = </var>
            <span>${func}</span>
        </div>
        `);
    };
    let eqs = [];

    if (_lastG.length > 0) {
        _lastG.forEach(g => {
            g.destroy();
            g.el.innerHTML = '';
        });
        _lastG.length = 0;
    }
    const POWER_TYPE = $("#eType").val();
    const F = $("#f").val() * 1;
    let T = 1 / F;
    if (T === Infinity) T = 0;

    getPowerEQ().then((Eeq) => {
        addEq(POWER_TYPE, Eeq);
        eqs.push(POWER_TYPE);
    }).then(() => {
        const
            type = getCurType(),
            R = getFirstComponentByType('R') || {},
            L = getFirstComponentByType('L') || {},
            C = getFirstComponentByType('C') || {};
        if (!type) {
            return new Promise(a => a([]));
        }
        let info = {
            type: type.id,
            R: R.value,
            L: L.value,
            C: C.value,
            il0: L.initValue,
            vc0: C.initValue,
        };

        return ajax("/getAlleq", {
            data: info,
            type: "POST",
            dataType: "json"
        }).then(arr => {
            if(!arr) return;
            arr.forEach(eq => {
                addEq(eq.key, eq.value);
                eqs.push(eq.key);
            });
            return arr;
        });
    }).then(() => {//Eval functions by step
        let p = [];
        eqs.forEach(eq => {
            p.push(
                GetFunctionPoints(eq, 0, T * 50, .005).then(ret => {
                    return {
                        data: ret,
                        label: eq
                    };
                })
            );
        });

        return Promise.all(p);
    }).then(pArr => {
        let EArr = pArr.filter(x => {
            const s = x.label.toLowerCase();
            return s.startsWith('v') || s.startsWith('e');
        });
        let IArr = pArr.filter(x => {
            const s = x.label.toLowerCase();
            return s.startsWith('i');
        });

        _lastG.push(drawPointArray(EArr, document.getElementById("fplot-v"), "Voltage", T));
        _lastG.push(drawPointArray(IArr, document.getElementById("fplot-i"), "Current", T));
    });
}

function drawPointArray(arr, container, title, t) {
    container.innerHTML = '';
    let nC = container.cloneNode(false);
    container.parentNode.insertBefore(nC, container);
    container.parentNode.removeChild(container);

    container = nC;
    let
        options,
        start,
        graph;
    options = {
        xaxis: {min: 0, max: 3 * t},
        title: title
    };

    // Draw graph with default options, overwriting with passed options
    function drawGraph(opts) {
        let __ret = {
            g: undefined
        };
        // Clone the options, so the 'options' variable always keeps intact.
        var o = Flotr._.extend(Flotr._.clone(options), opts || {});
        graph = Flotr.draw(
            container,
            arr,
            o
        );

        return graph;
    }

    // Actually draw the graph.
    graph = drawGraph();


    function initializeDrag(e) {
        start = graph.getEventPosition(e);
        Flotr.EventAdapter.observe(document, 'mousemove', move);
        Flotr.EventAdapter.observe(document, 'mouseup', stopDrag);
    }

    function move(e) {
        var
            end = graph.getEventPosition(e),
            xaxis = graph.axes.x,
            offset = start.x - end.x;
        if (xaxis.min + offset < 0) return;
        graph = drawGraph({
            xaxis: {
                min: xaxis.min + offset,
                max: xaxis.max + offset
            }
        });
        // @todo: refector initEvents in order not to remove other observed events
        Flotr.EventAdapter.observe(graph.overlay, 'mousedown', initializeDrag);
    }

    function stopDrag() {
        Flotr.EventAdapter.stopObserving(document, 'mousemove', move);
    }

    Flotr.EventAdapter.observe(graph.overlay, 'mousedown', initializeDrag);

    return graph;
}

$("#run").click(run);
//})();