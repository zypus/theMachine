package com.the.machine.debug;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 27/05/15
 */
public class ValueMapDebugger
		extends JFrame {

	private static final float SCALE = 3;
	private static Thread active;
	private static ValueMapDebugger[] windows= new ValueMapDebugger[10];
	private static DebugPanel[] debugPanels = new DebugPanel[10];

	private ValueMapDebugger(float[][] valuemap, int index) {
		Insets insets = getInsets();
		this.setBounds(0, 0, (int) (valuemap.length * SCALE) + insets.left + insets.right, (int) (valuemap[0].length * SCALE) + insets.top + insets.bottom);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("Mapper Debugger");
		DebugPanel debugPanel = new DebugPanel(valuemap, SCALE);
		this.setContentPane(debugPanel);
		debugPanels[index] = debugPanel;
		this.setVisible(true);
	}

	public static void debug(float[][] valuemap, List<Vector2> points, int index) {
		ValueMapDebugger window = windows[index];
		if (window == null) {
			window = new ValueMapDebugger(valuemap, index);
			windows[index] = window;
		}
		Insets insets = window.getInsets();
		DebugPanel debugPanel = debugPanels[index];
		debugPanel.valuemap = valuemap;
		debugPanel.points = points;
		window.setBounds(window.getX(), window.getY(), insets.left + insets.right + (int) (valuemap.length * SCALE), insets.top + insets.bottom + (int) (valuemap[0].length * SCALE));
		window.repaint();
	}

	private static class DebugPanel
			extends JPanel {

		public float[][] valuemap;
		public List<Vector2> points = new ArrayList<>();
		private float scale;

		public DebugPanel(float[][] valuemap, float scale) {
			this.valuemap = valuemap;
			this.scale = scale;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			Rectangle2D pixel = new Rectangle2D.Double(0, 0, 1, 1);
			// determine max value in the map
			int width = valuemap.length;
			int height = valuemap[0].length;
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (min > valuemap[i][j]) {
						min = valuemap[i][j];
					}
					if (max < valuemap[i][j]) {
						max = valuemap[i][j];
					}
				}
			}
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					float v = (valuemap[i][j] - min) / (max - min);
					g2.setColor((v < 0.5)
								? lerp(Color.BLUE, Color.GREEN, v / 0.5f)
								: lerp(Color.GREEN, Color.RED, (v - 0.5f) / 0.5f));
					pixel.setRect(i * scale, (height - 1 - j) * scale, 1 * scale, 1 * scale);
					g2.fill(pixel);
				}
			}
			if (points != null) {
				for (Vector2 point : points) {
					g2.setColor(Color.white);
					pixel.setRect(point.x * scale, (height - 1 - point.y) * scale, 1 * scale, 1 * scale);
					g2.fill(pixel);
				}
			}
		}

		private Color lerp(Color a, Color b, float t) {
			float ti = 1f - t;
			return new Color(MathUtils.clamp(a.getRed() / 255f * ti + b.getRed() / 255f * t, 0, 1), MathUtils.clamp(a.getGreen() / 255f * ti + b.getGreen() / 255f * t, 0, 1), MathUtils.clamp(a.getBlue() / 255f * ti + b.getBlue() / 255f * t, 0, 1), MathUtils.clamp(a.getAlpha() / 255f * ti + b.getAlpha() / 255f * t, 0, 1));
		}
	}
}
