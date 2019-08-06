package avorontsov.petridish;

import javafx.scene.control.Tab;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
 * An organizational structure to contain all the JavaFX GUI porridge that is contained within the Create tab of the simulation.
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
				
				// number input box (accepts and validates input)
				BoundedIntField simSpeedMsg = new BoundedIntField(PetriDishApp.MIN_SIMULATION_TICK_DELAY_MS, PetriDishApp.MAX_SIMULATION_TICK_DELAY_MS);	
				// simulation speed slider (any change is applied immediately)
				Slider simSpeed = new Slider(PetriDishApp.MIN_SIMULATION_TICK_DELAY_MS, PetriDishApp.MAX_SIMULATION_TICK_DELAY_MS, PetriDishApp.DEFAULT_SIMULATION_TICK_DELAY_MS);
				// pause/play button
				Button pause = new Button();
				
				// configure elements
					// text field
					simSpeedMsg.setText(app.simulationDelay + "");
					simSpeedMsg.setMaxWidth(50);
					
					// slider
					simSpeed.setBlockIncrement(1); // increments of 1 ms
					simSpeed.setMajorTickUnit(10);
					simSpeed.setMinorTickCount(1);
					simSpeed.setShowTickMarks(true);
					
					// button
					if (app.simulationPaused == true) { // initialize to configured state
						pause.setText("Play");
					} else {
						pause.setText("Pause");
					}
					
				// done configuring elements
					
				// add elements
					topBox.getChildren().add(simSpeed);
					topBox.getChildren().add(simSpeedMsg);
					topBox.getChildren().add(pause);
				// done adding elements
					
				// JavaFX listeners & events
					
				// text field updates the GUI state value and the slider when focus is lost
				simSpeedMsg.focusedProperty().addListener(new ChangeListener<Boolean>()
				{
				    @Override
				    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)
				    {
				        if (oldValue) // happens when focus changes from true to false -> focus lost
				        {
				        	// code in BoundedIntField validates user input, no validation code here
				        	// update internal value
				        	app.simulationDelay = simSpeedMsg.getInteger();
				        	// update the corresponding slider
							simSpeed.setValue(app.simulationDelay);
				        }
				    }
				});
				
				// slider updates the GUI state value and the text field whenever changed
				simSpeed.valueProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
						// slider ensures that the input is always valid, no validation code needed
						app.simulationDelay = (int) Math.round((double) newValue); // newValue will be double. int cast truncates, which is fine
						simSpeedMsg.setText(app.simulationDelay + ""); // update the text field
					}
					
				});
				
				// pause/play button toggles between two states
				pause.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						if (pause.getText().equals("Pause")) {
							pause.setText("Play");
							app.simulationPaused = true;
						} else {
							pause.setText("Pause");
							app.simulationPaused = false;
						}
						
					}
					
				});
				
	}
}
