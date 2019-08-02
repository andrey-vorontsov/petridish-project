package avorontsov.petridish;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * A modification of the JavaFX TextField class that accepts only inputs that
 * can be parsed to an integer within a set minimum and maximum (inclusive).
 * Retrieving its value with Integer.parseInt() is guaranteed to be safe.
 * 
 * Code adapted from https://gist.github.com/ricemery/4534910
 * 
 * @author Andrey Vorontsov
 */
public class BoundedIntField extends TextField {
	
	private int myInt; // the int represented by this field's text

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
	 * @throws IllegalArgumentException if min is not less than or equal to max
	 */
	public BoundedIntField(int min, int max) {
		if (min > max)
			throw new IllegalArgumentException("Cannot instantiate a BoundedIntField with invalid bounds.");
		
		// these listeners trigger before any listeners added on top of this class
		// so they ensure that validation occurs before external actors can access
		// the textProperty or the focusedProperty

		// if value stops being valid after an input, revert the change
		textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!isValid(newValue)) {
					setText(oldValue); // revert invalid changes
				}
				// excluding fringe cases that are valid but not valid enough, update myInt
				if (!getText().equals("-") && !(getText().length() == 0)) {
					myInt = Integer.parseInt(getText());
				} else {
					myInt = 0; // while the text doesn't match an int but is still ok to type, keep sentinel value
				}
			}
		});
		
		// when focus lost, force appropriate changes
		focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)
		    {
		        if (oldValue) // happens when focus changes from true to false -> focus lost
		        {
		        	// force myInt into bounds and correct it if invalid
		        	if (myInt < min)
		        		myInt = min;
		        	else if (myInt > max)
		        		myInt = max;
		        	// int has been placed in bounds
		        	// if the field lost focus while the state was "-" or "", it will snap to 0
		        	setText(myInt + "");
		        }
		    }
		});
	}

	/**
	 * Helper method to validate inputs.
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
	 * @return the fully validated and current value of this text field's input
	 */
	public int getInteger() {
		return myInt;
	}

}
