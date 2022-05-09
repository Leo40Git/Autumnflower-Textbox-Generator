var xlat = {
    1: "A\\s[<<<]1",
    2: "D\\s[<<<]2",
    3: "B\\s[<<<]3",
    4: "W\\s[<<<]4",
    5: "K\\s[<<<]5",
    8: "X\\s[<<<]8",
    a: "A\\s[<<<]1",
    b: "B\\s[<<<]3",
    c: "C\\s[<<<]3",
    d: "D\\s[<<<]2",
    e: "E\\s[<<<]1",
    f: "F\\s[<<<]4",
    g: "G\\s[<<<]2",
    h: "H\\s[<<<]4",
    i: "I\\s[<<<]1",
    j: "J\\s[<<<]8",
    k: "K\\s[<<<]5",
    l: "L\\s[<<<]1",
    m: "M\\s[<<<]3",
    n: "N\\s[<<<]1",
    o: "O\\s[<<<]1",
    p: "P\\s[<<<]3",
    q: "Q\\s[<<<]10",
    r: "R\\s[<<<]1",
    s: "S\\s[<<<]1",
    t: "T\\s[<<<]1",
    u: "U\\s[<<<]1",
    v: "V\\s[<<<]4",
    w: "W\\s[<<<]4",
    x: "X\\s[<<<]8",
    y: "Y\\s[<<<]4",
    z: "Z\\s[<<<]10"
};

function updateTextbox(faces, box) {
    var text = input.getString("Enter text (case insensitive, A-Z, 1-5 and 8 only):", "scrabble is fun");

    box.face = faces.get("Twelve", "Tiled");

    str = "Twelve:\n\\c[1]";
    for (var i = 0; i < text.length; i++) {
        var xlated = xlat[text.charAt(i).toLowerCase()];
        if (xlated === undefined) {
            continue;
        }
        str += xlated + " \\s";
    }
    box.text = str;
}
