var xlat = {
    a: "A₁",
    b: "B₃",
    c: "C₃",
    d: "D₂",
    e: "E₁",
    f: "F₄",
    g: "G₂",
    h: "H₄",
    i: "I₁",
    j: "J₈",
    k: "K₅",
    l: "L₁",
    m: "M₃",
    n: "N₁",
    o: "O₁",
    p: "P₃",
    q: "Q₁₀",
    r: "R₁",
    s: "S₁",
    t: "T₁",
    u: "U₁",
    v: "V₄",
    w: "W₄",
    x: "X₈",
    y: "Y₄",
    z: "Z₁₀"
};

function updateTextbox(faces, box) {
    var text = input.getString("Enter text (case insensitive):", "scrabble is fun");

    box.face = faces.get("Twelve", "Tiled");

    str = "Twelve:[br]\n[color=pal(9)]";
    for (var i = 0; i < text.length; i++) {
        var xlated = xlat[text.charAt(i).toLowerCase()];
        if (xlated === undefined) {
            str += text.charAt(i);
        } else {
            str += xlated;
        }
    }
    str += "[/color]";
    box.text = str;
}
