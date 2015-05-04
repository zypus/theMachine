package com.the.machine.debug;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/05/15
 */
public class MapDebugWindow
		extends JFrame {

	private static final float SCALE = 2;
	private static Thread active;

	private MapDebugWindow(float[][] map) {
		this.setBounds(0, 0, (int)(map.length*SCALE), (int)(map[0].length*SCALE));
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("Map Debugger");
		this.setContentPane(new DebugPanel(map, SCALE));
		this.setVisible(true);
	}

	public static void debug(float[][] map, float interval) {
		MapDebugWindow window = new MapDebugWindow(map);
		Thread updateLoop = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep((long) (interval * 1000));
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

	private static class DebugPanel extends JPanel {

		private float[][] map;
		private int width;
		private int height;
		private float scale;

		public DebugPanel(float[][] map, float scale) {
			this.map = map;
			width = map.length;
			height = map[0].length;
			this.scale = scale;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			Rectangle2D pixel = new Rectangle2D.Double(0,0,1,1);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					float v = map[i][height-1-j];
					if (v > 1) {
						g2.setColor(new Color(0.4117647f, 0.5019608f,1f));
					} else {
						g2.setColor(new Color(v, 0, 0));
					}
					pixel.setRect(i*scale, j*scale, 1*scale, 1*scale);
					g2.fill(pixel);
				}
			}
		}
	}
}
