package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.utility.Enums;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 18/02/15
 */
@Getter
@Accessors(chain = true)
public class TableCellComponent extends AbstractComponent {
	@Setter private Cell                      cell                = null;
	@Setter private boolean                   added               = false;
	private         Value                     minWidth            = Value.minWidth;
	private         Value                     prefWidth           = Value.prefWidth;
	private         Value                     maxWidth            = Value.maxWidth;
	private         Value                     minHeight           = Value.minHeight;
	private         Value                     prefHeight          = Value.prefHeight;
	private         Value                     maxHeight           = Value.maxHeight;
	private         int                       expandX             = 0;
	private         int                       expandY             = 0;
	private         float                     fillX               = 0;
	private         float                     fillY               = 0;
	private         Value                     spaceTop            = new Value.Fixed(0);
	private         Value                     spaceLeft           = new Value.Fixed(0);
	private         Value                     spaceBottom         = new Value.Fixed(0);
	private         Value                     spaceRight          = new Value.Fixed(0);
	private         Value                     padTop              = new Value.Fixed(0);
	private         Value                     padLeft             = new Value.Fixed(0);
	private         Value                     padBottom           = new Value.Fixed(0);
	private         Value                     padRight            = new Value.Fixed(0);
	private         boolean                   uniformX            = false;
	private         boolean                   uniformY            = false;
	private int                       colspan             = 1;
	private Enums.HorizontalAlignment horizontalAlignment = Enums.HorizontalAlignment.CENTER;
	private Enums.VerticalAlignment   verticalAlignment   = Enums.VerticalAlignment.CENTER;
	private boolean                   rowEnd              = false;

	public void apply() {
		if (cell != null) {
			cell.minSize(getMinWidth(), getMinHeight())
				.prefSize(getPrefWidth(), getPrefHeight())
				.maxSize(getMaxWidth(), getMinHeight());
			cell.expand(getExpandX(), getExpandY())
				.fill(getFillX(), getExpandY());
			cell.space(getSpaceTop(), getSpaceLeft(), getSpaceBottom(), getSpaceRight());
			cell.pad(getPadTop(), getPadLeft(), getPadBottom(), getPadRight());
			cell.uniform(isUniformX(), isUniformY());
			cell.colspan(getColspan());
			if (getHorizontalAlignment() == Enums.HorizontalAlignment.CENTER || getVerticalAlignment() == Enums.VerticalAlignment.CENTER) {
				cell.center();
			}
			if (getHorizontalAlignment() == Enums.HorizontalAlignment.LEFT) {
				cell.left();
			} else if (getHorizontalAlignment() == Enums.HorizontalAlignment.RIGHT) {
				cell.right();
			}
			if (getVerticalAlignment() == Enums.VerticalAlignment.TOP) {
				cell.top();
			} else if (getVerticalAlignment() == Enums.VerticalAlignment.BOTTOM) {
				cell.bottom();
			}
		}
	}

	public TableCellComponent setWidth(Value value) {
		minWidth = value;
		prefWidth = value;
		maxWidth = value;
		dirty = true;
		return this;
	}

	public TableCellComponent setHeight(Value value) {
		minHeight = value;
		prefHeight = value;
		maxHeight = value;
		dirty = true;
		return this;
	}

	public TableCellComponent setWidth(float value) {
		return setWidth(new Value.Fixed(value));
	}

	public TableCellComponent setHeight(float value) {
		return setHeight(new Value.Fixed(value));
	}

	public TableCellComponent setMinWidth(Value minWidth) {
		if (!minWidth.equals(this.minWidth)) {
			this.minWidth = minWidth;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setPrefWidth(Value prefWidth) {
		if (!prefWidth.equals(this.prefWidth)) {
			this.prefWidth = prefWidth;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setMaxWidth(Value maxWidth) {
		if (!maxWidth.equals(this.maxWidth)) {
			this.maxWidth = maxWidth;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setMinHeight(Value minHeight) {
		if (!minHeight.equals(this.minHeight)) {
			this.minHeight = minHeight;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setPrefHeight(Value prefHeight) {
		if (!prefHeight.equals(this.prefHeight)) {
			this.prefHeight = prefHeight;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setMaxHeight(Value maxHeight) {
		if (!maxHeight.equals(this.maxHeight)) {
			this.maxHeight = maxHeight;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setExpandX(int expandX) {
		if (this.expandX != expandX) {
			this.expandX = expandX;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setExpandY(int expandY) {
		if (this.expandY != expandY) {
			this.expandY = expandY;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setFillX(float fillX) {
		if (this.fillX != fillX) {
			this.fillX = fillX;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setFillY(float fillY) {
		if (this.fillY == fillY) {
			this.fillY = fillY;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setSpaceTop(Value spaceTop) {
		if (!spaceTop.equals(this.spaceTop)) {
			this.spaceTop = spaceTop;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setSpaceLeft(Value spaceLeft) {
		if (!spaceTop.equals(this.spaceTop)) {
			this.spaceLeft = spaceLeft;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setSpaceBottom(Value spaceBottom) {
		if (!spaceTop.equals(this.spaceTop)) {
			this.spaceBottom = spaceBottom;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setSpaceRight(Value spaceRight) {
		if (!spaceRight.equals(this.spaceRight)) {
			this.spaceRight = spaceRight;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setPadTop(Value padTop) {
		if (!padTop.equals(this.padTop)) {
			this.padTop = padTop;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setPadLeft(Value padLeft) {
		if (!padLeft.equals(this.padLeft)) {
			this.padLeft = padLeft;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setPadBottom(Value padBottom) {
		if (!padBottom.equals(this.padBottom)) {
			this.padBottom = padBottom;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setPadRight(Value padRight) {
		if (!padRight.equals(this.padRight)) {
			this.padRight = padRight;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setUniformX(boolean uniformX) {
		if (this.uniformX != uniformX) {
			this.uniformX = uniformX;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setUniformY(boolean uniformY) {
		if (this.uniformY != uniformY) {
			this.uniformY = uniformY;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setColspan(int colspan) {
		if (this.colspan != colspan) {
			this.colspan = colspan;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setHorizontalAlignment(Enums.HorizontalAlignment horizontalAlignment) {
		if (this.horizontalAlignment != horizontalAlignment) {
			this.horizontalAlignment = horizontalAlignment;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setVerticalAlignment(Enums.VerticalAlignment verticalAlignment) {
		if (this.verticalAlignment != verticalAlignment) {
			this.verticalAlignment = verticalAlignment;
			dirty = true;
		}
		return this;
	}

	public TableCellComponent setRowEnd(boolean rowEnd) {
		if (this.rowEnd != rowEnd) {
			this.rowEnd = rowEnd;
			dirty = true;
		}
		return this;
	}
}
