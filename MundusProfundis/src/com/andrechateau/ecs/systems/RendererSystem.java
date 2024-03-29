package com.andrechateau.ecs.systems;

import com.andrechateau.ecs.components.ActorSprite;
import com.andrechateau.ecs.components.Creature;
import com.andrechateau.ecs.components.Position;
import com.andrechateau.ecs.components.User;
import com.andrechateau.gamestates.Game;
import com.andrechateau.engine.Renderer;
import static com.andrechateau.gamestates.Game.msgRecord;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Mapper;
import com.artemis.utils.ImmutableBag;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Font;
import org.newdawn.slick.SlickException;

public class RendererSystem extends EntitySystem implements Renderer {

    private static Font font;
    private static Font fontOutlined;

    @Mapper
    ComponentMapper<Position> pm;

    @Mapper
    ComponentMapper<ActorSprite> as;

    @Mapper
    ComponentMapper<Creature> cr;

    @Mapper
    ComponentMapper<User> um;

    @SuppressWarnings("unchecked")
    public RendererSystem() {
        super(Aspect.getAspectForAll(Position.class, ActorSprite.class));
    }

    @Override
    protected void processEntities(ImmutableBag<Entity> entities) {
        try {
            if (font == null) {
                font = new AngelCodeFont("res/small_font.fnt", "res/small_font_0.tga");
            }
            if (fontOutlined == null) {
                fontOutlined = new AngelCodeFont("res/small_font_outlined.fnt", "res/small_font_outlined_0.tga");

            }
        } catch (SlickException ex) {
            Logger.getLogger(RendererSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        mapRenderer(entities);
        debugRenderer(entities);
        HUDRender();

    }

    @Override
    protected boolean checkProcessing() {
        return true;
    }

    @Override
    public void renderObjects(int visualY, int mapY, int layer, ImmutableBag<Entity> entities) {
        for (int i = 0; entities.size() > i; i++) {
            Entity e = entities.get(i);;
            Position position = pm.get(e);
            if (((int) position.getY() / 32 == mapY + 1) && layer == Game.map.getLayerIndex("64Wall")) {
                creatureRenderer(entities.get(i));
            }
        }
    }

    private void mapRenderer(ImmutableBag<Entity> entities) {
        Graphics g = Game.gc.getGraphics();
        int s = entities.size();
        Entity player = entities.get(0);
        Position playerpos = pm.get(player);
        int rx = (-(int) (playerpos.getX() % 32)) - 32;
        int ry = -(int) (playerpos.getY() % 32);
        int sx = (int) (playerpos.getX() / 32) - (Game.gc.getWidth() / 64) - 1;
        int sy = (int) (playerpos.getY() / 32) - (Game.gc.getHeight() / 64);
        int wx = (Game.gc.getWidth() / 32) + 2;
        int hy = (Game.gc.getWidth() / 32) + 1;
        Game.map.render(rx, ry, sx, sy, wx, hy, 0, false);
        Game.map.render(rx, ry, sx, sy, wx, hy, 1, true, entities);
        Game.map.render(rx, ry, sx, sy, wx, hy, 2, true, entities);
    }

    private void creatureRenderer(Entity e) {
        Entity player = Game.player.getEntity();
        Position playerpos = pm.get(player);
        Position position = pm.get(e);

        Game.gc.getGraphics()
                .setColor(Color.white);
        float x = -playerpos.getX() + position.getX() + Game.gc.getWidth() / 2;
        float y = -playerpos.getY() + position.getY() + Game.gc.getHeight() / 2;
        y -= 13;
        x -= 16;
        Creature c = cr.getSafe(e);
        boolean print = true;
        if (c.getName().equals(Game.player.getName())) {
            if ((um.getSafe(e) != null)) {
                print = true;
            } else {
                print = false;
            }
        }
        if (c != null) {
            if (print) {
                Font f = Game.gc.getGraphics().getFont();
                Game.gc.getGraphics().setFont(fontOutlined);
                int fontH = fontOutlined.getHeight(c.getName());
                int fontW = fontOutlined.getWidth(c.getName());
                Game.gc.getGraphics().setColor(Color.white);
                Game.gc.getGraphics().drawString(c.getName(), x, y - 25);
                Game.gc.getGraphics().setColor(new Color(0, 0, 0, 100));
                Game.gc.getGraphics().fillRect(x - 2, y - 10 - fontH, fontW + 2, fontH - 2);
                Game.gc.getGraphics().setFont(f);
                //Fonts.ttfbold.drawString(x, y - 25, c.getName(), Color.white);
                Game.gc.getGraphics().setColor(Color.black);
                Game.gc.getGraphics().fillRect(x, y - 10, 32, 4);
                Color color = Color.green;
                if (c.getHP() > 60 && c.getHP() <= 90) {
                    color = new Color(0, 200, 0);
                }
                if (c.getHP() > 30 && c.getHP() <= 60) {
                    color = new Color(200, 200, 0);
                }
                if (c.getHP() > 10 && c.getHP() <= 30) {
                    color = new Color(200, 0, 0);
                }
                if (c.getHP() <= 10) {
                    color = new Color(100, 0, 0);
                }
                Game.gc.getGraphics().setColor(color);
                Game.gc.getGraphics().fillRect(x + 1, y - 9, 30 * c.getHP() / 100, 2);
                Game.gc.getGraphics().drawRect(x, y, 32, 32);
                Game.gc.getGraphics().drawRect(x + 1, y + 1, 30, 30);

                as.get(e).getImage(position.getDirection()).draw(x - 32, y - 32, 64, 64);

            }
        } else {
            Game.gc.getGraphics().drawRect(x, y, 32, 32);
            Game.gc.getGraphics().drawRect(x + 1, y + 1, 30, 30);
            as.get(e).getImage(position.getDirection()).draw(x - 32, y - 32, 64, 64);
        }

    }

    private void debugRenderer(ImmutableBag<Entity> entities) {
        Graphics g = Game.gc.getGraphics();
        Position playerpos = pm.get(Game.player.getEntity());
        int walltilex = (int) (playerpos.getX() / 32 - 1);
        int walltiley = (int) playerpos.getY() / 32;
        if ((Game.map.getTileId(walltilex > 0 ? walltilex : 0, walltiley, 1)) == 0) {
            g.setColor(Color.green);
        } else {
            g.setColor(Color.red);
        }

        int sqrx = (Game.gc.getInput().getMouseX() + ((int) (playerpos.getX() / 32) - (Game.gc.getWidth() / 64) - 1) * 32 + 32);
        int sqry = (Game.gc.getInput().getMouseY() + ((int) (playerpos.getY() / 32) - (Game.gc.getHeight() / 64)) * 32);
        //g.drawString("X: " + ((int) playerpos.getX() / 32) + "   Y: " + ((int) playerpos.getY() / 32), 20, 60);
        int tileX = (int) (Game.gc.getInput().getMouseX() / 32 + (playerpos.getX() / 32) - (Game.gc.getWidth() / 64));
        int tileY = (int) (Game.gc.getInput().getMouseY() / 32 + (playerpos.getY() / 32) - (Game.gc.getHeight() / 64));
        //g.drawString("X: " + tileX + "   Y: " + tileY, 20, 75);
        Color c = Game.gc.getGraphics().getColor();
        Game.gc.getGraphics().setColor(Color.white);
        Game.gc.getGraphics().drawRect(Game.gc.getInput().getMouseX() - (Game.gc.getInput().getMouseX() % 32) - (playerpos.getX() % 32), Game.gc.getInput().getMouseY() - (Game.gc.getInput().getMouseY() % 32) - (playerpos.getY() % 32), 32, 32);
        Game.gc.getGraphics().setColor(c);
    }

    private void HUDRender() {
        Game.gc.getGraphics().setColor(new Color(0, 0, 0, 0.5f));
        Game.gc.getGraphics().setFont(font);
        Game.gc.getGraphics().fillRect(5, Game.gc.getHeight() - 30 - 155, 300, 155);
        Game.gc.getGraphics().setColor(Color.white);
        int listSize = Game.msgRecord.size();
        int maxsize = listSize >= 10 ? 10 : listSize;
        int end = listSize - 1;
        int init = listSize >= 10 ? listSize - 10 : 0;
        int k = maxsize;
        for (int i = end; i >= init; i--) {
            k--;
            Game.gc.getGraphics().drawString(msgRecord.get(i), 10, Game.gc.getHeight() - 30 + (k * 15) - (maxsize * 15));
        }
    }
}
