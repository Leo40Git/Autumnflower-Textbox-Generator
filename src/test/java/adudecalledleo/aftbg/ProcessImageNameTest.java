package adudecalledleo.aftbg;

import adudecalledleo.aftbg.app.dialog.FacePoolEditorDialog;

public class ProcessImageNameTest {
    public static void main(String[] args) {
        String[] cases = {
                "test.png", "a_really_long_test.png", "yet_another.png", "___a_stupid_name.png"
        };

        for (String aCase : cases) {
            System.out.format("%s -> %s%n", aCase, FacePoolEditorDialog.processImageName(aCase));
        }
    }
}
