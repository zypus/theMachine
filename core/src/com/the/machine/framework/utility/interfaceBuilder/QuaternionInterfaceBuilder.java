package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.components.canvasElements.TextFieldComponent;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.Enums;
import com.the.machine.framework.utility.InterfaceBuilder;
import com.the.machine.framework.utility.Interfacer;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class QuaternionInterfaceBuilder
		implements InterfaceBuilder {
	@Override
	public Entity interfaceFor(Interfacer interfacer, Object object, Class<?> aClass, Object owner, Field field) {
		Quaternion quaternion = (Quaternion) object;
		Entity table = new Entity();
		table.add(new TableComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT));
		table.add(new CanvasElementComponent());
		table.add(new TransformComponent());
		table.add(new DimensionComponent());
		SubEntityComponent entities = new SubEntityComponent();
		table.add(entities);
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeLabel("x"))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))));
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeTextField(new TextFieldComponent().setText("" + quaternion.getPitch())
																										 .setTextFieldListeners(new QuaternionListener(quaternion, "x"))
																										 .setTextFieldFilter(new FloatFilter())))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))
															   .setWidth(new Value.Fixed(60))));
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeLabel("y"))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))));
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeTextField(new TextFieldComponent().setText("" + quaternion.getYaw())
																										 .setTextFieldListeners(new QuaternionListener(quaternion, "y"))
																										 .setTextFieldFilter(new FloatFilter())))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))
															   .setWidth(new Value.Fixed(60))));
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeLabel("z"))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))));
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeTextField(new TextFieldComponent().setText("" + quaternion.getRoll())
																										 .setTextFieldListeners(new QuaternionListener(quaternion, "z"))
																										 .setTextFieldFilter(new FloatFilter())))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))
															   .setWidth(new Value.Fixed(60))));
		return table;
	}

	private static class QuaternionListener
			implements TextField.TextFieldListener {

		Quaternion quaternion;
		String     field;

		private QuaternionListener(Quaternion quaternion, String field) {
			this.quaternion = quaternion;
			this.field = field;
		}

		@Override
		public void keyTyped(TextField textField, char c) {
			float v;
			if (textField.getText()
						 .equals("") || textField.getText()
												 .equals("-")) {
				v = 0;
			} else {
				v = Float.parseFloat(textField.getText());
			}
			switch (field) {
				case "x":
					quaternion.setEulerAngles(quaternion.getAngleAround(0,1,0), v, quaternion.getAngleAround(0,0,1));
					break;
				case "y":
					quaternion.setEulerAngles(-v, quaternion.getAngleAround(1,0,0), quaternion.getAngleAround(0,0,1));
					break;
				case "z":
					quaternion.setEulerAngles(quaternion.getAngleAround(0,1,0), quaternion.getAngleAround(1,0,0), -v);
					break;
			}
		}
	}
}

