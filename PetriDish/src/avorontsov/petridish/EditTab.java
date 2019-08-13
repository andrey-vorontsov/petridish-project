package avorontsov.petridish;

import javafx.scene.control.Tab;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * An organizational structure to contain all the JavaFX GUI porridge that is
 * contained within the Create tab of the simulation.
 * 
 * @author Andrey Vorontsov
 */
public class EditTab extends Tab {

	/**
	 * Build the Create tab.
	 */
	public EditTab(PetriDishApp app) {
		setText("Edit");
		setClosable(false);

		// organized in a single VBox
		VBox createTabBox = new VBox();
		createTabBox.setPadding(new Insets(10, 5, 10, 5));
		createTabBox.setSpacing(10);
		createTabBox.setAlignment(Pos.TOP_CENTER);
		setContent(createTabBox);
		// done setting up box

		// organized into several stacked HBoxes with separators in between
		createTabBox.getChildren().add(new Separator());

		HBox topBox = new HBox();
		createTabBox.getChildren().add(topBox);
		topBox.setSpacing(10);
		topBox.setAlignment(Pos.CENTER_LEFT);

		createTabBox.getChildren().add(new Separator());

		HBox secondBox = new HBox(); // second box currently unused
		createTabBox.getChildren().add(secondBox);
		secondBox.setSpacing(10);
		secondBox.setAlignment(Pos.CENTER_LEFT);

		createTabBox.getChildren().add(new Separator());
		// finished setting up organization

		// begin adding GUI elements to their layout boxes

		// simulation speed text box and slider implementation
		// coupled; both elements update the other

		// input for simulation speed
		BoundedIntField simSpeedMsg = new BoundedIntField(PetriDishApp.MIN_SIMULATION_TICK_DELAY_MS,
				PetriDishApp.MAX_SIMULATION_TICK_DELAY_MS);
		// simulation speed slider
		Slider simSpeed = new Slider(PetriDishApp.MIN_SIMULATION_TICK_DELAY_MS,
				PetriDishApp.MAX_SIMULATION_TICK_DELAY_MS, PetriDishApp.DEFAULT_SIMULATION_TICK_DELAY_MS);
		// pause/play button
		Button pause = new Button();
		// input field for agar feed rate
		BoundedIntField agarFeedMsg = new BoundedIntField();
		// agar feed slider
		Slider agarFeed = new Slider(agarFeedMsg.getMinValue(), agarFeedMsg.getMaxValue(), PetriDishApp.DEFAULT_AGAR_FEED_FACTOR);

		// configure elements
		// text field for sim speed
		simSpeedMsg.setMaxWidth(50);

		// sim speed slider
		simSpeed.setBlockIncrement(1); // increments of 1 ms
		simSpeed.setMajorTickUnit(10);
		simSpeed.setMinorTickCount(1);
		simSpeed.setShowTickMarks(true);

		// button
		if (app.simulationPaused.get()) { // initialize to configured state
			pause.setText("Play");
		} else {
			pause.setText("Pause");
		}
		
		// text field for agar feed
		agarFeedMsg.setMaxWidth(50);
		
		// agar feed slider
		simSpeed.setBlockIncrement(1); // increments of 1
		simSpeed.setMajorTickUnit(10);
		simSpeed.setMinorTickCount(1);
		simSpeed.setShowTickMarks(true);

		// done configuring elements

		// add elements
		topBox.getChildren().add(simSpeed);
		topBox.getChildren().add(simSpeedMsg);
		topBox.getChildren().add(pause);
		
		secondBox.getChildren().add(agarFeed);
		secondBox.getChildren().add(agarFeedMsg);
		// done adding elements

		// JavaFX listeners & events

		// text field updates the GUI state value and through binding the slider
		simSpeedMsg.integerProperty().bindBidirectional(app.simulationDelay);

		// slider updates the internal value, which propagates to the text field
		simSpeed.valueProperty().bindBidirectional(app.simulationDelay);

		// pause/play button toggles between two states
		pause.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (pause.getText().equals("Pause")) {
					pause.setText("Play");
					app.simulationPaused.set(true);
				} else {
					pause.setText("Pause");
					app.simulationPaused.set(false);
				}

			}

		});
		
		// text field updates the GUI state value, propagating to the slider
		agarFeedMsg.integerProperty().bindBidirectional(app.runningAgarFeedFactor);
		
		// slider updates the internal value, which propagates to the text field
		agarFeed.valueProperty().bindBidirectional(app.runningAgarFeedFactor);

	}
}
