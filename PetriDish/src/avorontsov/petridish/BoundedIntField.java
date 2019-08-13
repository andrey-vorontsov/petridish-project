package avorontsov.petridish;

import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

/**
 * A modification of the JavaFX TextField class that accepts only inputs that
 * can be parsed to an integer within a set minimum and maximum (inclusive).
 * Its value should be retrieved using integerProperty(), which is guaranteed
 * to encode an integer in that range.
 * 
 * The text field defaults to 0 or whichever bound is closest to zero.
 * 
 * The field allows the user to input any string which can be parsed to an 
 * int using Integer.parseInt(), as well as the strings "-" and "" which
 * may occur as intermediate steps while typing. If anything else is typed,
 * the field will revert the change.
 * 
 * The property's value does not update until the user submits the change,
 * either by pressing enter or clicking out of the text field (the text
 * field loses focus). At this time, the bounds are applied (i.e. if the
 * parsed integer value is out of bounds when 
 * submitted, both the property and the displayed text snap to either
 * the max or the min as needed). Finally, if the Strings "-" or "" are
 * submitted, the text and the property default to 0 or whichever bound
 * is closest to zero.
 * 
 * @author Andrey Vorontsov
 */
public class BoundedIntField extends TextField {
	
	private SimpleIntegerProperty myInt; // the integer property, which will always be the sanitized version of the text

	private int min;
	private int max;
	
	/**
	 * Defaults are min = 0; max = 100
	 */
	public BoundedIntField() {
		this(0, 100);
	}

	/**
	 * Creates a BoundedIntField with a given minimum and maximum value. The field
	 * will accept only valid integer input between the two values (inclusive).
	 * 
	 * @param min the minimum value that this field will accept
	 * @param max the maximum value that this field will accept
	 * @throws IllegalArgumentException if min is greater than the max
	 */
	public BoundedIntField(int min, int max) {
		if (min > max)
			throw new IllegalArgumentException("Cannot instantiate a BoundedIntField with invalid bounds.");
		
		myInt = new SimpleIntegerProperty();
		this.min = min;
		this.max = max;
		
		// these listeners trigger before any listeners added on top of this class
		// so they ensure that validation occurs before external actors can access
		// the textProperty or the focusedProperty

		// if value stops being valid after an input, revert the change
		textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!isValid(newValue)) {
					setText(oldValue); // revert invalid inputs (not parseable ints or "" or "-")
				}
			}
		});
		
		// if the integer value is updated from outside or by another listener in this constructor
		// also update the text field
		myInt.addListener(new ChangeListener<Number>() {
			
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				setText(myInt.get() + "");
				
			}
		});
		
		// when focus lost, submit the change
		// when focus gained, select all text in the field
		focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)
		    {
		        if (oldValue) { // happens when focus changes from true to false -> focus lost
		        		
					// some cases are valid inputs but not valid values (out of range, "-", "" specifically)
					// in these cases, we need to force both the text value and the property into valid ranges
		        	
					if ((getText().equals("-")) || (getText().length() == 0)) {
						myInt.set(0);
					}
					
					if (Integer.parseInt(getText()) < min) { 
						myInt.set(min);
					} else if (Integer.parseInt(getText()) > max) {
						myInt.set(max);
					} else { // no invalidating condition applies, take the validated text as the value
						myInt.set(Integer.parseInt(getText()));
					}
        	
		        } else { // focus gained, select the contents of this field
		        	Platform.runLater(new Runnable() {
		        		// execute in a delayed runnable. If selectAll() is called here, the mouse action will immediately follow and click into the field, deselecting the text.
		        		// this way, the selection is done ASAP afterwards
		        		
						@Override
						public void run() {
							selectAll();
						}
		        		
		        	});
		        }
		    }
		});
		
		// pressing the enter key releases focus, which invokes the above listener as well and is equivalent
		setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					getParent().requestFocus(); // a kind of hacky way to release focus (using setFocused(false)) locks the user out of editing this field until they focus something else and come back
				}
			}
			
		});
	}

	/**
	 * Helper method to validate inputs while user is typing. If any user edit causes the String contents of the field to fail this check, the field reverts to the last valid state.
	 * 
	 * @param value the String to validate
	 * @return true only if the input is a valid integer or something that might be typed on the way to a valid integer
	 */
	private boolean isValid(String value) {
		if (value.length() == 0 || value.equals("-")) {
			return true;
		}

		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/**
	 * @return the fully validated integer value last submitted to this field
	 */
	public SimpleIntegerProperty integerProperty() {
		return myInt;
	}

	/**
	 * @return the min value this bounded int field can represent
	 */
	public int getMinValue() {
		return min;
	}
	
	/**
	 * @return the max value this bounded int field can represent
	 */
	public double getMaxValue() {
		return max;
	}

}
