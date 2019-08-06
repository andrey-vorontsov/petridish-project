package avorontsov.petridish;

import javafx.scene.control.Tab;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * An organizational structure to contain all the JavaFX GUI porridge that is contained within the Edit tab of the simulation.
 * 
 * @author Andrey Vorontsov
 */
public class CreateTab extends Tab {

	/**
	 * Build the Edit tab.
	 */
	public CreateTab(PetriDishApp app) {
		setText("Create");
		setClosable(false);

		// organized in a single VBox
			VBox editTabBox = new VBox();
			editTabBox.setPadding(new Insets(10, 5, 10, 5));
			editTabBox.setSpacing(10);
			editTabBox.setAlignment(Pos.TOP_CENTER);
			setContent(editTabBox);
		// done setting up box
			
		// organized into HBoxes, with separators
			editTabBox.getChildren().add(new Separator());
			
				HBox topBox = new HBox();
				editTabBox.getChildren().add(topBox);
				topBox.setSpacing(10);
				topBox.setAlignment(Pos.CENTER_LEFT);
			
			editTabBox.getChildren().add(new Separator());
			
				HBox secondBox = new HBox(); // second box currently unused
				editTabBox.getChildren().add(secondBox);
				secondBox.setSpacing(10);
				secondBox.setAlignment(Pos.CENTER_LEFT);
			
			editTabBox.getChildren().add(new Separator());
		// finished setting up organization
			
		// begin adding GUI elements to their layouts
			
				// width input box (validates input)
			BoundedIntField simDimWidthMsg = new BoundedIntField(PetriDishApp.MIN_PETRI_DISH_DIM, PetriDishApp.MAX_PETRI_DISH_DIM);	
				simDimWidthMsg.setText(app.newSimulationWidth + "");
				simDimWidthMsg.setMaxWidth(75);

				// int field updates the GUI state value when focus is lost
				simDimWidthMsg.focusedProperty().addListener(new ChangeListener<Boolean>()
				{
				    @Override
				    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)
				    {
				        if (oldValue) // happens when focus changes from true to false -> focus lost
				        {
				        	// update internal value
				        	app.newSimulationWidth = simDimWidthMsg.getInteger();
				        }
				    }
				});
				
				// two text fields encode dimensions of new simulation
				BoundedIntField simDimHeightMsg = new BoundedIntField(PetriDishApp.MIN_PETRI_DISH_DIM, PetriDishApp.MAX_PETRI_DISH_DIM);	
				simDimHeightMsg.setText(app.newSimulationHeight + "");
				simDimHeightMsg.setMaxWidth(75);

				// int field updates the GUI state value when focus is lost
				simDimHeightMsg.focusedProperty().addListener(new ChangeListener<Boolean>()
				{
				    @Override
				    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)
				    {
				        if (oldValue) // happens when focus changes from true to false -> focus lost
				        {
				        	// update internal value
				        	app.newSimulationHeight = simDimHeightMsg.getInteger();
				        }
				    }
				});
				
				topBox.getChildren().add(simDimWidthMsg);
				topBox.getChildren().add(simDimHeightMsg);
				
				// END OF SECOND
				
				// END OF CENTER COLUMN
				
				
				
	}
}
