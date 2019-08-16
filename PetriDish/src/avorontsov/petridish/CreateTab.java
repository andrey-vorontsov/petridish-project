package avorontsov.petridish;

import javafx.scene.control.Tab;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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
		VBox createTabBox = new VBox();
		createTabBox.setPadding(new Insets(10, 5, 10, 5));
		createTabBox.setSpacing(10);
		createTabBox.setAlignment(Pos.TOP_CENTER);
		setContent(createTabBox);
		// done setting up box

		// organized into HBoxes, with separators
		createTabBox.getChildren().add(new Separator());
		// each section labeled
		createTabBox.getChildren().add(new Label("New Simulation Size"));

		HBox topBox = new HBox();
		createTabBox.getChildren().add(topBox);
		topBox.setSpacing(10);
		topBox.setAlignment(Pos.CENTER_LEFT);

		createTabBox.getChildren().add(new Separator());
		createTabBox.getChildren().add(new Label("New Simulation Cell Pops"));

		HBox secondBox = new HBox();
		createTabBox.getChildren().add(secondBox);
		secondBox.setSpacing(10);
		secondBox.setAlignment(Pos.CENTER_LEFT);

		createTabBox.getChildren().add(new Separator());
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
		// with their own labels in VBoxes
		
		ArrayList<VBox> labelContainers = new ArrayList<VBox>();
		
		// make the boxes
		
		VBox simAgarPopMsgContainer = new VBox();
		VBox simGrazerPopMsgContainer = new VBox();
		VBox simPredPopMsgContainer = new VBox();
		VBox simPlantPopMsgContainer = new VBox();
		
		// configure the boxes
		
		labelContainers.add(simAgarPopMsgContainer);
		labelContainers.add(simGrazerPopMsgContainer);
		labelContainers.add(simPredPopMsgContainer);
		labelContainers.add(simPlantPopMsgContainer);
		
		for (VBox v : labelContainers) {
			v.setPadding(new Insets(10, 5, 10, 5));
			v.setSpacing(10);
			v.setAlignment(Pos.TOP_CENTER);
		}
		
		simAgarPopMsgContainer.getChildren().add(new Label("Agar"));
		simAgarPopMsgContainer.getChildren().add(simAgarPopMsg);
		
		simGrazerPopMsgContainer.getChildren().add(new Label("Grazer"));
		simGrazerPopMsgContainer.getChildren().add(simGrazerPopMsg);
		
		simPredPopMsgContainer.getChildren().add(new Label("Predator"));
		simPredPopMsgContainer.getChildren().add(simPredPopMsg);
		
		simPlantPopMsgContainer.getChildren().add(new Label("Plant"));
		simPlantPopMsgContainer.getChildren().add(simPlantPopMsg);
		
		for (VBox v : labelContainers) {
			secondBox.getChildren().add(v);
		}
		
	}
}
