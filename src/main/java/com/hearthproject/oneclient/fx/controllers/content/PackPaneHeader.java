package com.hearthproject.oneclient.fx.controllers.content;

import com.hearthproject.oneclient.fx.controllers.MainController;
import com.hearthproject.oneclient.fx.controllers.content.base.ContentPaneController;
import com.hearthproject.oneclient.json.models.launcher.ModPack;
import com.hearthproject.oneclient.util.launcher.PackUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PackPaneHeader extends ContentPaneController {
	public TextField searchBox;

	static boolean search;
	static String seachTerm;
	static boolean canceUpdate = false;

	static MainController staticController;
	public VBox packlistBox;

	@Override
	protected void onStart() {
		staticController = controller;
		updatePackList();
		searchBox.textProperty().addListener((observable, oldValue, newValue) -> updatePackList());
	}

	static Thread reloadThread = createUpdateThread();

	private static Thread createUpdateThread() {
		return new Thread(() -> {
			try {

				try {
					for (ModPack modPack : PackUtil.loadModPacks().packs) {
						if (canceUpdate) {
							break;
						}
						if (search) {
							//TODO REGEX as someone will ask for it when then dont even need it
							if (!modPack.name.toLowerCase().contains(seachTerm.toLowerCase()) && !modPack.description.toLowerCase().contains(seachTerm.toLowerCase()) && !modPack.authors.toLowerCase().contains(seachTerm.toLowerCase())) {
								continue;
							}
						}
						if (canceUpdate) {
							break;
						}
						Platform.runLater(() -> addPackCard(modPack));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				OneClientLogging.log(e);
			}
		});
	}

	public void updatePackList() {
		search = !searchBox.getText().isEmpty();
		seachTerm = searchBox.getText();

		Node sNode = controller.contentPane.getChildren().get(0);
		controller.contentPane.getChildren().removeIf(node -> node != sNode);
		canceUpdate = true;
		try {
			reloadThread.join();
		} catch (InterruptedException e) {
			OneClientLogging.log(e);
		}
		canceUpdate = false;
		reloadThread = createUpdateThread();
		reloadThread.start();
	}

	@Override
	public void refresh() {

	}

	public static void addPackCard(ModPack modPack) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/modpacklist/packcard.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.log("An error has occurred loading the mod pack card!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			staticController.contentPane.getChildren().add(fxmlLoader.load(fxmlUrl.openStream()));
			PackCardController packCardController = fxmlLoader.getController();
			packCardController.modpackName.setText(modPack.name);
			packCardController.modpackDetails.setText(modPack.authors);
			packCardController.modpackDescription.setText(modPack.description);
			if (modPack.iconImage != null) {
				packCardController.modpackImage.setImage(modPack.iconImage);
			}

		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}
}
