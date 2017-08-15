package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.mashape.unirest.http.Unirest;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;

public class LogController {
	public TextArea logArea;

	private Stage stage;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void menuUpload(ActionEvent actionEvent) {
		try {
			String key = Unirest.post("https://hastebin.com/documents").body(logArea.getText()).asJson().getBody().getObject().getString("key");
			String url = "https://hastebin.com/" + key + ".hs";
			OneClientLogging.log("Log has been uploaded to: " + url);
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
			}
		} catch (Throwable e) {
			OneClientLogging.log(e);
		}
	}

	public void menuClose(ActionEvent actionEvent) {
		stage.hide();
	}
}