package at.bay.twitcher

import groovy.json.JsonSlurper
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
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

	@FXML
	private ComboBox<Stream> streamCombo;

	@FXML
	private ComboBox<Quality> qualityCombo

	private ObservableList<Stream> streamList = FXCollections.observableArrayList()

	private FilteredList<Stream> filteredStreamList = new FilteredList<>(streamList)

	def updateStreamList = {
		String text = 'https://api.twitch.tv/kraken/streams?limit=100'.toURL().text
		def json = new JsonSlurper().parseText(text)
		streamList.clear()
		json.streams.each { stream ->
			streamList.add(new Stream(name: stream.channel.name, game: stream.channel.game, url: stream.channel.url, status: stream
					.channel.status, viewers: stream.viewers))
		}
	}

	@Override
	void initialize(URL location, ResourceBundle resources) {
		streamCombo.setItems(filteredStreamList)

		initUpdateThread()
		updateStreamList()


		def filterStreamsByInput = { ev
			->
			filteredStreamList.setPredicate {
				it.toString().toLowerCase().contains(streamCombo.getEditor().getText().toLowerCase())
			}
			streamCombo.show()
		}
		streamCombo.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, filterStreamsByInput)

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
						sprintf('Name: %1$-20.20s | Status: %2$-25.25s | Game: %3$-30.30s | Viewer: %4$-6s', object.name, object.status, object.game,
								object.viewers)
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

	private Thread initUpdateThread() {
		Thread.startDaemon {
			while (true) {
				sleep(300000)
				updateStreamList()
			}
		}
	}

	@FXML
	private void watch() {
		new ProcessBuilder('livestreamer', "${streamCombo.getValue().url}", "${qualityCombo.getValue().toString().toLowerCase()}").start()
	}

}
