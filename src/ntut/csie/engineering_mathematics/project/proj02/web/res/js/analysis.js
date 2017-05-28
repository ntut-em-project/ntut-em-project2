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
            node: 2,
        }
    },
];

function getCurConds() {
    let ret = {
        R: 0,
        L: 0,
        C: 0,
        node: 0
    };

    AllComponent.forEach(c => {
        if (ret[c.type] !== undefined) ret[c.type]++;
    });

    ret.node = AllNodeCollection.length;

    return ret;
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

function getEeq() {
    let info = {
        kind: $("#eKind").val(),
        f: $("#f").val(),
        vpp: $("#vpp").val(),
        vdc: $("#vdc").val(),
    };

    return ajax("/getEeq", {
        data: info,
        type: "POST",
        dataType: "json"
    });
}

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

    getEeq().then((Eeq) => {
        addEq("E", Eeq);
        eqs.push("E");
    }).then(() => {//Eval functions by step
        let p = [];
        eqs.forEach(eq => {
            p.push(
                GetFunctionPoints(eq, 0, 10, .01).then(ret => {
                    return {
                        data: ret,
                        label: eq
                    };
                })
            );
        });

        return Promise.all(p);
    }).then(pArr => {
        drawPointArray(pArr);
    });
}

function drawPointArray(arr) {
    (function mouse_drag(container) {

        let
            options,
            graph,
            start,
            i;


        options = {
            selection: {mode: 'x', fps: 30},
            title: 'Result'
        };

        // Draw graph with default options, overwriting with passed options
        function drawGraph(opts) {

            // Clone the options, so the 'options' variable always keeps intact.
            var o = Flotr._.extend(Flotr._.clone(options), opts || {});

            // Return a new graph.
            return Flotr.draw(
                container,
                arr,
                o
            );
        }

        // Actually draw the graph.
        graph = drawGraph();

        // Hook into the 'flotr:select' event.
        Flotr.EventAdapter.observe(container, 'flotr:select', function (area) {

            // Draw graph with new area
            graph = drawGraph({
                xaxis: {min: area.x1, max: area.x2},
                yaxis: {min: area.y1, max: area.y2}
            });
        });

        // When graph is clicked, draw the graph with default area.
        Flotr.EventAdapter.observe(container, 'flotr:click', function () {
            drawGraph();
        });

    })(document.getElementById("fplot"));
}

$("#run").click(run);
//})();