function updateTextbox(faces, box) {
    var addFace = input.getBoolean("Add face to textbox?");
    if (addFace == null) {
        return;
    }
    var textLength = input.getInt("Enter character count:");
    if (textLength == null) {
         return;
    }

    box.face = addFace ? faces.get("Sixty", "Neutral") : faces.blank;
    var lnLim = addFace ? 22 : 28;

    str = "Sixty:\n[color=pal(10)]";
    lnCnt = 0;
    for (var i = 0; i < textLength; i++) {
        str += "\u2591";
        lnCnt++;
        if (lnCnt >= lnLim) {
            str += "[br]\n";
            lnCnt = 0;
        }
    }
    str += "[/color]"
    box.text = str;
}

