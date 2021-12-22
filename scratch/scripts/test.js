function updateTextbox(faces, box) {
    var aNumber = input.getInt("Enter some random number:", 42);
    if (aNumber == null) {
        return;
    }

    box.face = faces.get("Mercia", "Neutral");
    box.text = "Mercia:\n\\c[25]Hold on. " + aNumber;
}
