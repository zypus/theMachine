package com.the.machine.debug;

import com.the.machine.map.Mapper;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 22/05/15
 */
public class MapperDebugWindow
		extends JFrame {

	private static final float SCALE = 2;
	private static Thread active;

	private MapperDebugWindow(Mapper mapper) {
		Insets insets = getInsets();
		this.setBounds(0, 0, (int) (mapper.getWidth() * SCALE) + insets.left + insets.right, (int) (mapper.getWidth()* SCALE) + insets.top + insets.bottom);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("Mapper Debugger");
		this.setContentPane(new DebugPanel(mapper, SCALE));
		this.setVisible(true);
	}

	public static void debug(Mapper mapper, float interval) {
		MapperDebugWindow window = new MapperDebugWindow(mapper);
		Thread updateLoop = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep((long) (interval * 1000));
						Insets insets = window.getInsets();
						window.setBounds(window.getX(), window.getY(), insets.left + insets.right + (int) (mapper.getWidth() * SCALE), insets.top + insets.bottom + (int) (mapper.getHeight() * SCALE));
						window.repaint();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		updateLoop.start();
		active = updateLoop;
	}

	private static class DebugPanel
			extends JPanel {

		private Mapper mapper;
		private float scale;

		public DebugPanel(Mapper mapper, float scale) {
			this.mapper = mapper;
			this.scale = scale;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			Rectangle2D pixel = new Rectangle2D.Double(0, 0, 1, 1);
			// determine max value in the map
			int width = mapper.getWidth();
			int height = mapper.getHeight();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					Mapper.MapTile tile = mapper.getMap().get(i).get(height - 1 - j);
					switch (tile.getAreaType()) {
						case OUTER_WALL:
							g2.setColor(new Color(0,0,0));
							break;
						case GROUND:
							g2.setColor(new Color(195, 195, 195));
							break;
						case WALL:
							g2.setColor(new Color(0, 0, 0));
							break;
						case WINDOW:
							g2.setColor(new Color(75, 150, 255));
							break;
						case WINDOW_BROKEN:
							g2.setColor(new Color(0, 6, 84));
							break;
						case DOOR_OPEN:
							g2.setColor(new Color(255, 125, 35));
							break;
						case DOOR_CLOSED:
							g2.setColor(new Color(71, 31, 9));
							break;
						case TARGET:
							g2.setColor(new Color(35, 255, 25));
							break;
						case TOWER:
							g2.setColor(new Color(255, 50, 25));
							break;
						case COVER:
							g2.setColor(new Color(104, 0, 200));
							break;
						case UNSEEN:
							g2.setColor(new Color(255, 92, 248));
							break;
					}
					pixel.setRect(i * scale, j * scale, 1 * scale, 1 * scale);
					g2.fill(pixel);
				}
			}
		}
	}
}
