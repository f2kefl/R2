package ru.FL.r2;

import javafx.animation.FillTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class r2Main extends Application
{
    private static final String JSON_DATA = "JsonData.json";
    private static List<Movie> listMovie = new ArrayList<>(); // список в формате Pane
    private static List<JSONObject> listJson = new ArrayList<>(); // список в формате JsonObject
    private static JSONArray jsonArray = null; // Json массив для сохранения и извлечения списка из файла
    private static JSONParser parser = new JSONParser();
    private static FileWriter fileWriter = null;

    private static Color[] colors = new Color[5];

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        colors[0] = Color.web("#ffeb99", 0.5); // default
        colors[1] = Color.web("#a6a6a6", 0.5); // драмы
        colors[2] = Color.web("#ff4d4d", 0.5); // боевики/триллеры/ужасы
        colors[3] = Color.web("#ff8900", 0.5); // комедии
        colors[4] = Color.web("#66b3ff", 0.5); // фантастика/фэнтези

        try {
            jsonArray = (JSONArray) parser.parse(new FileReader(JSON_DATA));
            System.out.println(jsonArray.toJSONString());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        listJson.addAll(jsonArray);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Pane root = new Pane();

        Image bg = new Image(this.getClass().getResourceAsStream("bg2.jpg"));
        ImageView imageView = new ImageView(bg);

        Btn add = new Btn("Add");
        Btn getRandom = new Btn("Random movie");
        Btn delete = new Btn("Delete");
        Btn toViewedList = new Btn("To Viewed");
        Button Info = new Button("Guide");
        Button showViewed = new Button("Viewed");

        add.setTranslateX(16+130);
        getRandom.setTranslateX(210+135);
        delete.setTranslateX(408+135);
        toViewedList.setTranslateX(605+135);

        Info.setTranslateX(930);
        Info.setTranslateY(5);
        Info.setFocusTraversable(false);
        showViewed.setFocusTraversable(false);
        showViewed.setTranslateX(985);
        showViewed.setTranslateY(5);

        showViewed.setOnMouseClicked(event -> viewed(new Stage()));
        Info.setOnMouseClicked(event -> info(new Stage()));

        add.setOnMouseClicked(event -> {
            String name = JOptionPane.showInputDialog(null, "Input new title");
            if (name != null && name.length() > 0) {
                JSONObject newFilm = new JSONObject();
                newFilm.put("name", name);
                newFilm.put("x", 5);
                newFilm.put("y", 5);
                newFilm.put("color", 0);
                newFilm.put("list", 1);
                listJson.add(newFilm);
                Movie m = new Movie();
                m.readJson(newFilm);
                listMovie.add(m);
                root.getChildren().add(m);
            }
        });

        getRandom.setOnMouseClicked(event -> {
            int crutch = 0;
            while (crutch < listMovie.size()) {
                crutch++;
                int i = (int) (Math.random() * listMovie.size());
                if ((!listMovie.get(i).isRandom) && ((long) listJson.get(i).get("list") == 1)) {
                    listMovie.get(i).getThis();
                    listMovie.get(i).isRandom = true;
                    break;
                }
            }
        });

        delete.setOnMouseClicked(event -> listMovie.forEach(movie -> {
            if (movie.isSelected) {
                listJson.remove(listMovie.indexOf(movie));
                root.getChildren().remove(movie);
                listMovie.remove(movie);
            }
        }));

        toViewedList.setOnMouseClicked(event -> listMovie.forEach(movie -> {
            if (movie.isSelected) {
                listJson.get(listMovie.indexOf(movie)).put("list", 2);
                root.getChildren().remove(movie);
            }
        }));

        root.getChildren().addAll(imageView, add, getRandom, delete, showViewed, Info, toViewedList);

        for (JSONObject film : listJson) {
            Movie m = new Movie();
            m.readJson(film);
            listMovie.add(m);
            if ((long) film.get("list") == 1) {
                root.getChildren().add(m);
            }
        }

        Scene scene = new Scene(root, 1038, 760);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                listMovie.forEach(movie -> {
                    try {
                        if (movie.isSelected) {
                            Long i = (Long) listJson.get(listMovie.indexOf(movie)).get("color");
                            if (i < colors.length - 1) {
                                i++;
                            }
                            else while (i > 0) {
                                i--;
                            }
                            listJson.get(listMovie.indexOf(movie)).put("color", i);
                            movie.rectangle.setFill(colors[Math.toIntExact(i)]);
                        }
                    } catch (Exception e) {
                        if (movie.isSelected) {
                            int i = (int) listJson.get(listMovie.indexOf(movie)).get("color");
                            if (i < colors.length - 1) {
                                i++;
                            }
                            else while (i > 0) {
                                i--;
                            }
                            listJson.get(listMovie.indexOf(movie)).put("color", i);
                            movie.rectangle.setFill(colors[i]);
                        }
                    }
                });
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("Random Movie");
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(event -> {
            try {
                jsonArray.clear();
                jsonArray.addAll(listJson);
                fileWriter = new FileWriter(JSON_DATA);
                fileWriter.write(jsonArray.toJSONString());
                fileWriter.flush();
                fileWriter.close();
                primaryStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public class Movie extends StackPane
    {
        GaussianBlur gb = new GaussianBlur(19.0D);
        DropShadow shadow = new DropShadow(5, Color.web("#000000"));

        Label label;
        Rectangle rectangle;
        boolean isSelected = false;
        boolean isRandom = false;

        Movie()
        {
            shadow.setBlurType(BlurType.THREE_PASS_BOX);

            label = new Label();
            label.setFont(new Font("Impact", 14));
            label.setStyle("-fx-effect: dropshadow(three-pass-box, black, 2, 0.5, 1, 1)");
            label.setTextFill(Color.web("#ffffff"));

            rectangle = new Rectangle(10, 22);

            rectangle.setStyle("-fx-effect: dropshadow(gaussian, rgba(6,7,7,0.25), 3, 0.6, 1, 1)");
            this.getChildren().addAll(rectangle, label);

            FillTransition ft = new FillTransition(Duration.seconds(0.5), rectangle);


            setOnContextMenuRequested(event -> {
                Color color = (Color) rectangle.getFill();
                if (!isSelected) {
                    ft.setFromValue(color);
                    ft.setToValue(Color.web("000000", 0.5D));
                    ft.setCycleCount(-1);
                    ft.setAutoReverse(true);
                    ft.play();
                    isSelected = true;
                }
                else {
                    rectangle.setFill(ft.getFromValue());
                    ft.stop();
                    isSelected = false;
                }
            });

            setOnMouseDragged(event -> {
                setTranslateX(event.getX() + getTranslateX());
                setTranslateY(event.getY() + getTranslateY());
                listJson.get(listMovie.indexOf(this)).put("x", (int) (event.getX() + getTranslateX()));
                listJson.get(listMovie.indexOf(this)).put("y", (int) (event.getY() + getTranslateY()));
            });
        }

        private void readJson(JSONObject o)
        {
            label.setText((String) o.get("name"));
            rectangle.setWidth(label.getText().length() > 7 ? label.getText().length() * 8 : label.getText().length() * 10);
            try {
                long x = (long) o.get("x");
                long y = (long) o.get("y");
                setTranslateX(x);
                setTranslateY(y);
                long c = (long) o.get("color");
                rectangle.setFill(colors[Math.toIntExact(c)]);
            } catch (Exception e) {
                setTranslateX((Integer) o.get("x"));
                setTranslateY((Integer) o.get("y"));
                rectangle.setFill(colors[(int) o.get("color")]);
            }
        }

        private void getThis()
        {
            rectangle.setFill(Color.FORESTGREEN);
            rectangle.setEffect(gb);
        }
    }

    private class Btn extends StackPane
    {
        Btn(String name)
        {
            GaussianBlur gb = new GaussianBlur(12.0D);

            setPrefSize(100, 30);
            setTranslateY(690);

            Rectangle bg = new Rectangle(180.0D, 50.0D, Color.web("#f2f2f2", 0.5D));
            bg.setEffect(gb);
            Label label = new Label(name);
            label.setFont(new Font("Arial Black", 15));
            label.setStyle("-fx-effect: dropshadow(three-pass-box, white, 2, 0.5, 1, 1)");
            label.setTextFill(Color.web("#262626"));

            FillTransition ft = new FillTransition(Duration.seconds(1), bg);
            this.setOnMouseEntered((event) -> {
                ft.setFromValue(Color.web("#f2f2f2", 0.5D));
                ft.setToValue(Color.web("FF0000", 0.5D));
                ft.setCycleCount(-1);
                ft.setAutoReverse(true);
                ft.play();
            });
            this.setOnMouseExited((event) -> {
                bg.setFill(ft.getFromValue());
                ft.stop();
            });
            this.getChildren().addAll(bg, label);
        }
    }

    private void info(Stage stage)
    {
        Pane pane = new Pane();

        Text text = new Text();
        text.setText("Краткая инструкция:\n"+
        "1. ПКМ выделяет объект. Повторное нажатие снимает выделение. Можно выделять несколько объектов;\n" +
                "2. To Viewed отправляет выделенные объекты в другое окно, которое можно вызвать по нажатию Viewed;\n" +
                "3. Пробел меняет цвет объекта;\n" +
                "4. С зажатой ЛКМ можно передвигать объекты по окну;\n" +
                "4. Кнопки пробел, Delete и To Viewed действуют только на выделенные объекты.");
        text.setFont(new Font("Impact", 14));
        text.setStyle("-fx-effect: dropshadow(three-pass-box, black, 2, 0.5, 1, 1)");
        text.setFill(Color.web("#ffffff"));
        text.setTranslateX(5);
        text.setTranslateY(25);

        pane.getChildren().add(text);

        Scene scene = new Scene(pane, 650, 150);
        stage.setScene(scene);
        stage.setTitle("Info");
        stage.setResizable(false);
        stage.show();
    }

    private void viewed(Stage stage)
    {
        Pane pane = new Pane();

        Image bg = new Image(this.getClass().getResourceAsStream("bg.png"));
        ImageView imageView = new ImageView(bg);

        Btn del = new Btn("Delete");
        del.setTranslateX(330);

        del.setOnMouseClicked(event -> listMovie.forEach(movie -> {
            if (movie.isSelected) {
                listJson.remove(listMovie.indexOf(movie));
                pane.getChildren().remove(movie);
                listMovie.remove(movie);
            }
        }));

        pane.getChildren().addAll(imageView, del);

        listMovie.forEach(movie -> {
            try {
                if ((long) listJson.get(listMovie.indexOf(movie)).get("list") == 2) {
                    pane.getChildren().add(movie);
                }
            } catch (Exception e) {
                if ((int) listJson.get(listMovie.indexOf(movie)).get("list") == 2) {
                    pane.getChildren().add(movie);
                }
            }
        });

        Scene scene = new Scene(pane, 790, 790);
        stage.setScene(scene);
        stage.setTitle("Viewed");
        stage.setResizable(false);
        stage.show();
    }
}


