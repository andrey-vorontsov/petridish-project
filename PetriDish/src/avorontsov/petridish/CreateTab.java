package avorontsov.petridish;

import javafx.scene.control.Tab;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * An organizational structure to contain all the JavaFX GUI porridge that is
 * contained within the Edit tab of the simulation.
 * 
 * @author Andrey Vorontsov
 */
public class CreateTab extends Tab {

	/**
	 * Build the Create tab.
	 * 
	 * @param app needed to reference default values for this instance of the Petri
	 *            Dish App
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

		HBox secondBox = new HBox();
		editTabBox.getChildren().add(secondBox);
		secondBox.setSpacing(10);
		secondBox.setAlignment(Pos.CENTER_LEFT);

		editTabBox.getChildren().add(new Separator());
		// finished setting up organization

		// begin adding GUI elements to their HBoxes

		// width input box
		BoundedIntField simDimWidthMsg = new BoundedIntField(PetriDishApp.MIN_PETRI_DISH_DIM,
				PetriDishApp.MAX_PETRI_DISH_DIM);
		simDimWidthMsg.setMaxWidth(75);

		simDimWidthMsg.integerProperty().bindBidirectional(app.newSimulationWidth);

		// height input box
		BoundedIntField simDimHeightMsg = new BoundedIntField(PetriDishApp.MIN_PETRI_DISH_DIM,
				PetriDishApp.MAX_PETRI_DISH_DIM);
		simDimHeightMsg.setMaxWidth(75);

		simDimHeightMsg.integerProperty().bindBidirectional(app.newSimulationHeight);

		topBox.getChildren().add(simDimWidthMsg);
		topBox.getChildren().add(simDimHeightMsg);

		// input fields for starting cell populations
		
		// agar
		BoundedIntField simAgarPopMsg = new BoundedIntField();
		simAgarPopMsg.setMaxWidth(50);

		simAgarPopMsg.integerProperty().bindBidirectional(app.newSimulationAgarPop);
		
		// grazer
		BoundedIntField simGrazerPopMsg = new BoundedIntField();
		simGrazerPopMsg.setMaxWidth(50);

		simGrazerPopMsg.integerProperty().bindBidirectional(app.newSimulationGrazerPop);
		
		// pred
		BoundedIntField simPredPopMsg = new BoundedIntField();
		simPredPopMsg.setMaxWidth(50);

		simPredPopMsg.integerProperty().bindBidirectional(app.newSimulationPredPop);
		
		// plant
		BoundedIntField simPlantPopMsg = new BoundedIntField();
		simPlantPopMsg.setMaxWidth(50);

		simPlantPopMsg.integerProperty().bindBidirectional(app.newSimulationPlantPop);
		
		// add those input boxes to the second box
		
		secondBox.getChildren().add(simAgarPopMsg);
		secondBox.getChildren().add(simGrazerPopMsg);
		secondBox.getChildren().add(simPredPopMsg);
		secondBox.getChildren().add(simPlantPopMsg);
	}
}
