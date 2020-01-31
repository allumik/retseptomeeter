package ee.receptometer;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Math.log;
import static java.lang.Math.round;

public class Main extends Application {
    // TODO: Should maybe somehow run in the headless mode? Not sure if necessary. Probably is.
    // TODO: Run somehow without specifying main class in the gradle build? Not sure if necessary.
    // TODO: extract it somehow to object creation and extracting 'image' to stream or to static method.

    //pane siis need väärtused hiljem sisendlahtritest (või käsurealt) määratavateks
    //NOTE: 24.04 added the kadiscore value
    static private int axis = 800; //size of canvas
    static private double postrec = 0.035; //double 0-1
    static private double prerec = 0.0299; //double 0-1
    static private double rec = 0.426; //double 0-1
    static private double kadiscore = 360; //int 0-1000
    static private double recrange = 0.4; //scale of the receptometer
    static private boolean kadiVa = false; //boolean to control kadiscore input
    static private boolean reclog = false; //logarithmic scale
    static private boolean noNum = false;
    static private boolean noCol = false;
    static private boolean recInd = false;
    static private boolean perc = false;
    static private String outPut = "retseptomeeter.png";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            double yaxis = axis / 3 * 2;
            HBox box = new HBox();
            box.setPrefWidth(axis + axis / 5);

            Canvas canv = new Canvas(axis, yaxis);
            GraphicsContext gc = canv.getGraphicsContext2D();

            // test cli :)
            if (getCLI()) {
                joonista(gc);
                try {
                    System.out.println("Printing out file" + this.outPut);
                    printOut(canv, this.outPut);
                } catch (RuntimeException s) {
                    System.out.println("Tekkis viga salvestamisel.");
                    s.printStackTrace();
                }
                box.getChildren().add(canv);

                Scene scene = new Scene(box, axis, yaxis);

                primaryStage.setTitle("Receptometer v0.2.521");
                primaryStage.setScene(scene);
                primaryStage.show();
                primaryStage.close();
            } else {
                //sidelined toolbar
                VBox toolBar = new VBox(4);

                toolBar.setPrefWidth(axis / 5);

                //Label header = new Label("Sisendid: ");

                Label post = new Label("Post-rec: ");
                Label pre = new Label("Pre-rec: ");
                Label rece = new Label("Receptivity: ");
                Label range = new Label("Range: ");
                Label kadi = new Label("KadiScore™: ");

                Separator sep = new Separator();
                Separator sep2 = new Separator();

                //TextField axisField = new TextField();
                //axisField.setPromptText("Picture size");

                TextField prerecField = new TextField();
                prerecField.setPromptText("Pre-rec value");

                TextField posrecField = new TextField();
                posrecField.setPromptText("Post-rec value");

                TextField recField = new TextField();
                recField.setPromptText("Receptivity");

                TextField kadiField = new TextField();
                kadiField.setPromptText("KadiScore™ value:");

                TextField recrangeField = new TextField();
                recrangeField.setPromptText("Scale start");

                //ToggleButton reclogField = new ToggleButton("Linear");

                ToggleButton noNum = new ToggleButton("Non-numbered scale");

                //ToggleButton perc = new ToggleButton("Scale percentages");

                ToggleButton noCol = new ToggleButton("Colorful hand");

                ToggleButton recIndic = new ToggleButton("Receptivity label");

                Button button2 = new Button("Plot it!");
                button2.setOnAction(actionEvent -> {
                    gc.clearRect(0, 0, axis, yaxis);
                    try {
                        //this.axis = parseInt(axisField.getText());
                        this.prerec = prerecField.getText().isEmpty() ? 0 : parseDouble(prerecField.getText());
                        this.postrec = posrecField.getText().isEmpty() ? 0 : parseDouble(posrecField.getText());
                        this.rec = (recField.getText().isEmpty()) ? 1 : parseDouble(recField.getText());
                        this.recrange = (recrangeField.getText().isEmpty()) ? 1 : parseDouble(recrangeField.getText());
                        //24.04.19 added kadiscore parts
                        this.kadiVa = !kadiField.getText().isEmpty();
                        this.kadiscore = kadiVa ? parseDouble(kadiField.getText()) : 360;
                        //this.reclog = reclogField.isSelected();
                        //if (reclog) reclogField.setText("Log");
                        //else reclogField.setText("Linear");
                        this.noNum = noNum.isSelected();
                        this.noCol = noCol.isSelected();
                        this.recInd = recIndic.isSelected();
                        //this.perc = perc.isSelected();
                        joonista(gc);
                    } catch (Exception e) {
                        gc.setStroke(Color.FIREBRICK);
                        gc.strokeText("Vale sisend!", axis / 2, yaxis / 2, axis / 5 * 3);
                    }
                });

                //faili väljakirjutamine
                TextField outFile = new TextField();
                outFile.setText("failinimi.png");

                Button button1 = new Button("Salvesta");
                button1.setOnAction(actionEvent -> {
                    try {
                        printOut(canv, outFile);
                    } catch (RuntimeException s) {
                        Label viga = new Label("ei tööta...");
                        toolBar.getChildren().add(viga);
                        s.printStackTrace();
                    }
                });

                //toolBar.getChildren().add(axisField);
                toolBar.getChildren().add(pre);
                toolBar.getChildren().add(prerecField);
                toolBar.getChildren().add(post);
                toolBar.getChildren().add(posrecField);
                toolBar.getChildren().add(rece);
                toolBar.getChildren().add(recField);
                toolBar.getChildren().add(range);
                toolBar.getChildren().add(recrangeField);
                toolBar.getChildren().add(kadi);
                toolBar.getChildren().add(kadiField);
                //toolBar.getChildren().add(reclogField);
                toolBar.getChildren().add(noNum);
                toolBar.getChildren().add(noCol);
                toolBar.getChildren().add(recIndic);
                //toolBar.getChildren().add(perc);
                toolBar.getChildren().add(button2);
                toolBar.getChildren().add(sep2);
                toolBar.getChildren().add(outFile);
                toolBar.getChildren().add(button1);

                box.getChildren().add(toolBar);
                box.getChildren().add(canv);

                Scene scene = new Scene(box, box.getPrefWidth(), yaxis);

                primaryStage.setTitle("Receptometer v0.2.521");
                primaryStage.setScene(scene);
                primaryStage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printOut(Canvas canv, String outFile) {
        File file = new File(outFile);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        WritableImage wim = new WritableImage(axis, Math.round(Math.round(axis / 3 * 2)));
        canv.snapshot(sp, wim);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printOut(Canvas canv, TextField outFile) {
        printOut(canv, outFile.getText());
    }

    // this goes to another branch
    public void getNoGUI(int score) {
        Map<String, String> params = new HashMap();

        params.put("kadiScore", String.valueOf(score));
        // [(--post=, --pre=) | (--kadiScore=), --rec=] --recrange=, --out=, --noNum=, --recInd=
        this.kadiVa = params.containsKey("kadiScore");
        if (params.containsKey("kadiScore")) {
            this.kadiVa = true;
            this.kadiscore = Integer.parseInt(params.get("kadiScore")); //int 0-200
        } else {
            this.postrec = Double.parseDouble(params.get("post")); //double 0-1
            this.prerec = Double.parseDouble(params.get("pre")); //double 0-1
        }
        this.rec = (params.containsKey("rec")) ? Double.parseDouble(params.get("rec")) : 1; //double 0-1
        //scale of the receptometer
        this.recrange = (params.containsKey("recrange")) ? Double.parseDouble(params.get("recrange")) : 1;
        if (params.containsKey("noNum")) this.noNum = params.get("noNum").equals("true");
        if (params.containsKey("out")) this.outPut = params.get("out");
        if (params.containsKey("recInd")) this.recInd = params.get("recInd").equals("true");
    }

    public boolean getCLI() {
        Application.Parameters args = getParameters();

        Map<String, String> params = args.getNamed();
        // [(--post=, --pre=) | (--kadiScore=), --rec=] --recrange=, --out=, --noNum=, --recInd=
        if (params.size() >= 2) {
            this.kadiVa = params.containsKey("kadiScore");
            if (params.containsKey("kadiScore")) {
                this.kadiVa = true;
                this.kadiscore = Integer.parseInt(params.get("kadiScore")); //int 0-200
            } else {
                this.postrec = Double.parseDouble(params.get("post")); //double 0-1
                this.prerec = Double.parseDouble(params.get("pre")); //double 0-1
            }
            this.rec = (params.containsKey("rec")) ? Double.parseDouble(params.get("rec")) : 1; //double 0-1
            //scale of the receptometer
            this.recrange = (params.containsKey("recrange")) ? Double.parseDouble(params.get("recrange")) : 1;
            if (params.containsKey("noNum")) this.noNum = params.get("noNum").equals("true");
            if (params.containsKey("out")) this.outPut = params.get("out");
            if (params.containsKey("recInd")) this.recInd = params.get("recInd").equals("true");
            return true;
        }
        return false;
    }

    public void joonista(GraphicsContext gc) {
        //insert skaalanäidikud here
        double iter = 0;
        double radius = axis / 20 * 9; //raadius
        double sygavus = 0.88;

        Font font = new Font("Lucida Console", axis / 40);
        gc.setFont(font);

		/*//seieri tausta taust
		gc.setFill(Color.DARKGRAY);
		gc.fillArc(axis/20, axis/20, axis/10*9, axis/10*9, 0, 180, ArcType.ROUND);*/

        //retseptomeetri alumine osa
        gc.setFill(Color.rgb(230, 230, 230));
        gc.fillRect(axis / 20, axis / 2, axis / 10 * 9, axis / 19);

        //seieri taust ekstreemsused (hall):
        if (kadiVa) {
            gc.setFill(Color.rgb(254, 254, 215));
            gc.fillArc(axis / 20, axis / 20, axis / 10 * 9, axis / 10 * 9, 0, 180, ArcType.ROUND);
        } else {
            gc.setFill(Color.rgb(230, 230, 230));
            gc.fillArc(axis / 20, axis / 20, axis / 10 * 9, axis / 10 * 9, 0, 180, ArcType.ROUND);
        }

        //seieri tausta medium tsoon (kollane):
        gc.setFill(Color.rgb(254, 254, 215));
        gc.fillArc(axis / 20, axis / 20, axis / 10 * 9, axis / 10 * 9,
                Math.toDegrees(radius * (0.3)) / (radius) * Math.PI / 2,
                Math.toDegrees(radius * (1.4)) / (radius) * Math.PI / 2, ArcType.ROUND);

        //seieri tausta tsoon roheline
        gc.setFill(Color.rgb(192, 255, 171));
        gc.fillArc(axis / 20, axis / 20, axis / 10 * 9, axis / 10 * 9,
                Math.toDegrees(radius * (0.7)) / (radius) * Math.PI / 2,
                Math.toDegrees(radius * (0.6)) / (radius) * Math.PI / 2, ArcType.ROUND);

        //insert täpsusskaalanäidikud here
        if (!kadiVa) {
            iter = recrange;
            double radiusPrec = axis / 10 * 4.5; //raadius
            double sygavusPrec = 0.75;
            gc.setStroke(Color.rgb(50, 50, 50));

            for (double i = 0.7; i <= 1.01; i = i + 0.05) {
                gc.setLineWidth(axis / 200);
                double rads = (radius * (i)) / (radius) * Math.PI / 2;

                gc.strokeLine(
                        axis / 2 + sygavusPrec * radiusPrec * Math.cos(rads),
                        axis / 2 - sygavusPrec * radiusPrec * Math.sin(rads),
                        axis / 2 + radiusPrec * Math.cos(rads),
                        axis / 2 - radiusPrec * Math.sin(rads));
                gc.strokeLine(
                        axis / 2 - sygavusPrec * radiusPrec * Math.cos(rads),
                        axis / 2 - sygavusPrec * radiusPrec * Math.sin(rads),
                        axis / 2 - radiusPrec * Math.cos(rads),
                        axis / 2 - radiusPrec * Math.sin(rads));

                if ((((double) round(1000 * i)) / 100) / 10 == 1) {
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeLine(axis / 2,
                            axis / 2,
                            axis / 2 - sygavusPrec * radiusPrec * Math.cos(rads),
                            axis / 2 - sygavusPrec * radiusPrec * Math.sin(rads));
                }
            }
        }

        //taustanumbrid ja jooned:
        gc.setStroke(Color.rgb(50, 50, 50));
        gc.setFill(gc.getStroke());
        gc.setLineWidth(axis / 200);
        font = new Font("Lucida Console", axis / 30);
        Font font2 = new Font("Lucida Console", axis / 40);
        double multi = 1.06;
        gc.setFont(font);

        for (double i = 1; i > -0.001; ) {
            double rads = (reclog) ?
                    log(radius * (i)) / (radius) * Math.PI / 3 :
                    (radius * (i)) / (radius) * Math.PI / 2;

            // kui on iga 1/10 möödas.
            if ((((double) round(10000 * i)) / 100) % 20 == 0 &&
                    ((double) round(1000 * i)) / 1000 != 1 &&
                    ((double) round(1000 * i)) / 1000 >= 0.1) {
                // loo seierimõõdikud
                gc.strokeLine(
                        axis / 2 + sygavus * radius * Math.cos(rads),
                        axis / 2 - sygavus * radius * Math.sin(rads),
                        axis / 2 + radius * Math.cos(rads),
                        axis / 2 - radius * Math.sin(rads));
                gc.strokeLine(
                        axis / 2 - sygavus * radius * Math.cos(rads),
                        axis / 2 - sygavus * radius * Math.sin(rads),
                        axis / 2 - radius * Math.cos(rads),
                        axis / 2 - radius * Math.sin(rads));
                // taustanumber normaalselt viiendiku tagant (0.2, 0.4, ...)
                // kui noNum on false
                // ja kui kadiVa on false
                if (!noNum && !kadiVa) {
                    // Kui on taustanumbrit vaja mis pole kadiscore
                    String txt = Double.toString(Math.round(iter * 100.0) / 100.0);
                    gc.fillText(txt,
                            axis / 2 - multi * radius * Math.cos(rads) - axis / 20 * 0.38 * Math.pow((1 - i), 0.9),
                            axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                            axis / 20);
                    gc.fillText(txt,
                            axis / 2 + multi * radius * Math.cos(rads) - axis / 20 * 0.32 * Math.pow((1 - i), 0.3),
                            axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                            axis / 20);
                    // kui on kadiscore sisestatud
                } else if (!noNum && kadiVa) {
                    gc.fillText(Integer.toString((int) (100 - Math.round(iter * 100))),
                            axis / 2 - multi * radius * Math.cos(rads) - axis / 20 * 0.56 * Math.pow((1 - i), 0.4),
                            axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                            axis / 20);
                    gc.fillText(Integer.toString((int) (100 + Math.round(iter * 100))),
                            axis / 2 + multi * radius * Math.cos(rads) - axis / 12 * 0.32 * Math.pow((1 - i), 0.3),
                            axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                            axis / 20);
                }
            } else {
                // joonista kümnendjooned
                sygavus = (i >= 0.9 && kadiVa) ? 0.91 : 0.88;
                gc.strokeLine(
                        axis / 2 + (sygavus + 0.05) * radius * Math.cos(rads),
                        axis / 2 - (sygavus + 0.05) * radius * Math.sin(rads),
                        axis / 2 + radius * Math.cos(rads),
                        axis / 2 - radius * Math.sin(rads));
                gc.strokeLine(
                        axis / 2 - (sygavus + 0.05) * radius * Math.cos(rads),
                        axis / 2 - (sygavus + 0.05) * radius * Math.sin(rads),
                        axis / 2 - radius * Math.cos(rads),
                        axis / 2 - radius * Math.sin(rads));
                sygavus = 0.88;
                if (((double) round(100 * i)) / 100 == 1) {
                    // kirjuta ülemise 100 või 0 numbri
                    if (!noNum) {
                        String txt = (kadiVa) ?
                                Integer.toString(100 - Math.round(((int) iter) * 100)) :
                                Double.toString(Math.round(iter * 100.0) / 100.0);
                        gc.fillText(txt,
                                axis / 2 - multi * radius * Math.cos(rads) - axis / 16 * 0.4,
                                axis / 2 - multi * radius * Math.sin(rads) + axis / 100,
                                axis / 15);
                    }
                } else if (((double) round(1000 * i)) / 1000 < 0.1) {
                    // kirjuta äärmised 0 või 200 numbri
                    if (!noNum) {
                        gc.fillText(Integer.toString((int) (100 - Math.round(iter * 100))),
                                axis / 2 - multi * radius * Math.cos(rads) - axis / 20 * 0.20,
                                axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                                axis / 20);
                        gc.fillText(Integer.toString((int) (100 + Math.round(iter * 100))),
                                axis / 2 + multi * radius * Math.cos(rads) - axis / 12 * 0.32 * Math.pow((1 - i), 0.3),
                                axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                                axis / 20);
                    }
                } else if (!noNum) {
                    // kirjuta kümnendikke vasakul poolel
                    gc.setFont(font2);
                    String txt = (kadiVa) ?
                            Integer.toString((int) (100 - Math.round(iter * 100))) :
                            Double.toString(Math.round(iter * 100.0) / 100.0);
                    gc.fillText(txt,
                            axis / 2 - multi * radius * Math.cos(rads) - axis / 20 * 0.32 * Math.pow((1 - i), 0.4),
                            axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                            axis / 20);
                    // kirjuta kümnendikke paremal pool keskjoonest
                    if (kadiVa)
                        txt = Integer.toString((int) (100 + Math.round(iter * 100)));
                    gc.fillText(txt,
                            axis / 2 + multi * radius * Math.cos(rads) - axis / 20 * 0.32 * Math.pow((1 - i), 0.1),
                            axis / 2 - multi * radius * Math.sin(rads) + axis / 100 * Math.pow((1 - i), 0.05),
                            axis / 20);
                    gc.setFont(font);
                }
            }
            i = (reclog) ? log(i - 0.1) : i - 0.1;
            iter = iter + (recrange) / 10;
        }

        /*
         * //seierimõõdikute tausta tsoon2
         * gc.setFill(Color.GREY);
         * gc.fillArc(axis/20, axis/20, axis/10*9, axis/10*9,
         * 		Math.toDegrees(radius*(0.7))/(radius)*Math.PI/2,
         * 		Math.toDegrees(radius*(0.6))/(radius)*Math.PI/2, ArcType.ROUND);
         */

        //tekstid
        gc.setFill(Color.rgb(50, 50, 50));
        //if (postrec>prerec) gc.setFill(Color.rgb(48, 154, 32));
        gc.fillText("PRE-RECEPTIVE", axis / 20 * 3.2, axis / 20 * 9.75, axis / 20 * 6);
        //gc.setFill(Color.rgb(100, 100, 100));
        //if (postrec<prerec) gc.setFill(Color.rgb(48, 154, 32));
        gc.fillText("POST-RECEPTIVE", axis / 20 * 11.8, axis / 20 * 9.75, axis / 20 * 6);

        if (recInd) {
            gc.setFill(Color.rgb(38, 134, 22));
            gc.setFont(new Font(gc.getFont().getName(), axis / 30));
            gc.fillText("RECEPTIVE", axis / 2 - axis / 20 * 1.60, axis / 9, axis / 20 * 8);
        }

        //seieri kinnitusnupp
        gc.setFill(Color.rgb(230, 230, 230));
        gc.fillArc(axis / 2 - axis / 20, axis / 10 * 4.5, axis / 10, axis / 10, 0, 360, ArcType.ROUND);

        /* Arvutuskäik:
         * Skaala jookseb -1 .. 0 .. 1
         * Seega arvutatakse vahe postrec - prerec, mis annab väärtuse skaalal (deltarec).
         * KadiScore™ puhul on tingimuslik skoorimine, kus postrec on default 0,
         * seega antakse väärtus deltareciga 1 .. 2.
         * Veaennetuseks on deltarec default ka 0, seega vea korral viskab rec'i.
         */

        double deltarec = 0;
        int mult = 1;
        if (kadiVa) {
            int minThresh = 70;
            int maxThresh = 130;
            int maxMax = 200;
            // scale into 0..2
            deltarec = kadiscore < minThresh ? 0.7 * kadiscore / minThresh :
                    (kadiscore > maxThresh ? 2 * kadiscore / maxMax : 1.3 * kadiscore / maxThresh);
            // send to range -1 .. 1
            deltarec--;
        } else {
            deltarec = Math.abs(postrec - prerec);
            mult = (postrec > prerec) ? 1 : -1;
            if (Math.abs(deltarec) > recrange) deltarec = recrange;
        }
        double seieriPaksus = axis / 25;
        radius -= axis / 80 - seieriPaksus / 2 + 70; // raadius + seierile kuluv ruum
        double rads = radius * (1 - deltarec / recrange) / (radius) * Math.PI / 2;

        //double rads = radius*(1-deltarec)/(radius)*Math.PI/2; working version w/o recrange

        //seier
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(seieriPaksus);
        gc.setStroke(Color.rgb(50, 50, 50));
        gc.strokeLine(axis / 2, axis / 2,
                axis / 2 + mult * radius * Math.cos(rads),
                axis / 2 - radius * Math.sin(rads));

        seieriPaksus -= 8;
        gc.setLineWidth(seieriPaksus);
        gc.setStroke(Color.rgb(70, 70, 70));
        gc.strokeLine(axis / 2, axis / 2,
                axis / 2 + mult * radius * Math.cos(rads),
                axis / 2 - radius * Math.sin(rads));

        //seierivärv
        if (noCol)
            gc.setStroke(Color.rgb(90, 90, 90));
        else if (rec <= 0.5 && rec > 0)
            gc.setStroke(Color.color(1, rec, 0));
        else if (rec > 0.5)
            gc.setStroke(Color.color(1.1 - rec, 0.9, 0.1));
        else
            gc.setStroke(Color.rgb(100, 101, 100));
        gc.setLineWidth(seieriPaksus);
        gc.strokeLine(axis / 2, axis / 2,
                axis / 2 + mult * rec * radius * Math.cos(rads),
                axis / 2 - rec * radius * Math.sin(rads));
    }

}