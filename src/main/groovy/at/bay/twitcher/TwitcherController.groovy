package at.bay.twitcher

import groovy.json.JsonSlurper
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.input.KeyEvent
import javafx.util.Callback
import javafx.util.StringConverter


/**
 * Created by pbayer.*/
class TwitcherController implements Initializable {

	static private List<Stream> streams = []

	@FXML
	private ComboBox<Stream> streamCombo;

	@FXML
	private ComboBox<Quality> qualityCombo
	private List<Stream> streamList

	@Override
	void initialize(URL location, ResourceBundle resources) {
		String text = 'https://api.twitch.tv/kraken/streams?limit=100'.toURL().text
		def json = new JsonSlurper().parseText(text)
		streamList = streams
		json.streams.each { stream ->
			streamList.add(new Stream(name: stream.channel.name, game: stream.channel.game, url: stream.channel.url, status: stream
					.channel.status, viewers: stream.viewers))
		}

		streamCombo.setItems(FXCollections.observableList(streamList))
		streamCombo.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, { nV
			->
			streamCombo.setItems(FXCollections.observableList(streamList.findAll({
				it.toString().toLowerCase().contains(streamCombo.getEditor().getText().toLowerCase())
			})))
			streamCombo.show()
		})
		streamCombo.setConverter(new StringConverter<Stream>() {

			@Override
			String toString(Stream s) {
				return s?.getName()
			}

			@Override
			Stream fromString(String string) {
				return streamList.find({
					it.getName().equals(string)
				})
			}
		})

		streamCombo.setCellFactory(new Callback<ListView<Stream>, ListCell<Stream>>() {

			@Override
			ListCell<Stream> call(ListView<Stream> param) {
				return new TextFieldListCell<Stream>(new StringConverter<Stream>() {

					@Override
					String toString(Stream object) {
						object.toString()
					}

					@Override
					Stream fromString(String string) {
						return null
					}
				})
			}
		})

		Quality.each { q -> qualityCombo.getItems().add(q) }
		qualityCombo.setValue(Quality.BEST)
	}

	@FXML
	private void watch() {
		new ProcessBuilder('livestreamer.exe', "${streamCombo.getValue().url}", "${qualityCombo.getValue().toString().toLowerCase()}").directory(new
				File('c:\\program files (x86)\\livestreamer')).start()
	}

}
