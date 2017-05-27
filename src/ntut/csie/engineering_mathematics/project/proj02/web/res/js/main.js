"use strict";
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

function TestGetPoints(){
    GetFunctionPoints("t^2", -5, 5, .5);
    GetFunctionPoints("exp(-t^2)", -5, 5, .5);
}
