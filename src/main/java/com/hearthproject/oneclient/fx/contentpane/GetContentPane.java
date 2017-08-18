package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.fx.controllers.PackCardController;
import com.hearthproject.oneclient.json.models.curse.CursePacks;
import com.hearthproject.oneclient.json.models.launcher.ModPack;
import com.hearthproject.oneclient.util.curse.CursePackUtil;
import com.hearthproject.oneclient.util.launcher.PackUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;

public class GetContentPane extends ContentPane {
	static boolean search;
	static String seachTerm;
	static boolean canceUpdate = false;
	static Thread reloadThread = createUpdateThread();
	public TextField searchBox;

	public GetContentPane() {
		super("gui/contentpanes/modpacklist/packlistheader.fxml", "Get Content", "#3A54A3");
	}

	private static Thread createUpdateThread() {
		return new Thread(() -> {
			try {

				try {
					int size = 0;
					for (CursePacks.CursePack modPack : CursePackUtil.loadModPacks().packs) {
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
						if(size++ > 25){
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

	public static void addPackCard(CursePacks.CursePack modPack) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/contentpanes/modpacklist/packcard.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.log("An error has occurred loading the mod pack card!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			Main.mainController.contentBox.getChildren().add(fxmlLoader.load(fxmlUrl.openStream()));
			PackCardController packCardController = fxmlLoader.getController();
			packCardController.modpackName.setText(modPack.name);
			packCardController.modpackDetails.setText(modPack.authors);
			packCardController.modpackDescription.setText(modPack.description);
			packCardController.pack = modPack;
//			if (modPack.iconImage != null) {
//				packCardController.modpackImage.setImage(modPack.iconImage);
//			}

		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}

	@Override
	protected void onStart() {
		updatePackList();
		searchBox.textProperty().addListener((observable, oldValue, newValue) -> updatePackList());
	}

	public void updatePackList() {
		search = !searchBox.getText().isEmpty();
		seachTerm = searchBox.getText();

		Node sNode = Main.mainController.contentBox.getChildren().get(0);
		Main.mainController.contentBox.getChildren().removeIf(node -> node != sNode);
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
}
