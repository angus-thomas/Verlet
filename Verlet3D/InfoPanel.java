package Verlet3D;
import javax.swing.*;
import java.awt.*;
public class InfoPanel extends JFrame{
	private final String INFO_TEXT=
	"<html>Materials modelling simulation using the Verlet algorithm<br/><br/>"
	+ "<ul> Extra information:"
	+ "<li>Colour represents depth in the Z direction; particles with warmer colours nearer the front."
	+ "<li>Starting the simulation without enough space for the particles (radius too large) will cause the simulation to break; set the particle radius to low before resetting."
	+ "<li>In case the simulation breaks, the reset button should fix it!"
	+ "<li>The solid border button toggles between periodic boundaries and a solid boundary.</ul></html>";
	public InfoPanel(){
		super("Info");
		setLayout(new GridBagLayout());
		setSize(600,300);

		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(20,20,20,20);
		c.weightx=0.9;
		c.weighty=0.9;
		c.fill=GridBagConstraints.BOTH;
		add(new JLabel(INFO_TEXT), c);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
}