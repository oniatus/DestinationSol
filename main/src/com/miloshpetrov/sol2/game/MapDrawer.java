package com.miloshpetrov.sol2.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.miloshpetrov.sol2.*;
import com.miloshpetrov.sol2.common.Col;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.maze.Maze;
import com.miloshpetrov.sol2.game.maze.MazeBuilder;
import com.miloshpetrov.sol2.game.planet.*;
import com.miloshpetrov.sol2.game.ship.*;
import com.miloshpetrov.sol2.ui.UiDrawer;

import java.util.ArrayList;

public class MapDrawer {
  public static final float MIN_ZOOM = 8f;
  public static final float MUL_FACTOR = 2f;
  public static final float MAX_ZOOM = 512f;
  public static final float ICON_RAD = .02f;
  public static final float STAR_NODE_SZ = .003f;
  private static final float MAX_SKULL_TIME = .75f;
  private static final float MAX_AREA_SKULL_TIME = 2;
  public static final float INNER_ICON_PERC = .6f;
  public static final float GRID_SZ = 40f;
  public static final String MAP_TEX_DIR = "mapObjs/";

  private final TextureAtlas.AtlasRegion myAtmTex;
  private final TextureAtlas.AtlasRegion myPlanetTex;
  private final TextureAtlas.AtlasRegion myPlanetCoreTex;
  private final TextureAtlas.AtlasRegion myStarTex;
  private final TextureAtlas.AtlasRegion myMazeTex;
  private final TextureAtlas.AtlasRegion mySkullTex;
  private final TextureAtlas.AtlasRegion mySkullBigTex;
  private final TextureAtlas.AtlasRegion myStarPortTex;
  private final TextureAtlas.AtlasRegion myBeltTex;
  private final TextureAtlas.AtlasRegion myBeaconAttackTex;
  private final TextureAtlas.AtlasRegion myBeaconMoveTex;
  private final TextureAtlas.AtlasRegion myBeaconFollowTex;
  private final TextureAtlas.AtlasRegion myIconBg;
  private final TextureAtlas.AtlasRegion myWarnAreaBg;
  private final TextureAtlas.AtlasRegion myWhiteTex;
  private final Color myAreaWarnCol;
  private final Color myAreaWarnBgCol;

  private boolean myToggled;
  private float myZoom;
  private float mySkullTime;
  private float myAreaSkullTime;

  public MapDrawer(TexMan texMan) {
    myIconBg = texMan.getTex(TexMan.HULL_ICONS_DIR + "bg", null);
    myWarnAreaBg = texMan.getTex(MAP_TEX_DIR + "warnBg", null);
    myAtmTex = texMan.getTex(MAP_TEX_DIR + "atm", null);
    myPlanetTex = texMan.getTex(MAP_TEX_DIR + "planet", null);
    myPlanetCoreTex = texMan.getTex(MAP_TEX_DIR + "planetCore", null);
    myStarTex = texMan.getTex(MAP_TEX_DIR + "star", null);
    myMazeTex = texMan.getTex(MAP_TEX_DIR + "maze", null);
    mySkullTex = texMan.getTex(TexMan.HULL_ICONS_DIR + "skull", null);
    mySkullBigTex = texMan.getTex(MAP_TEX_DIR + "skullBig", null);
    myStarPortTex = texMan.getTex(TexMan.HULL_ICONS_DIR + "starPort", null);
    myBeltTex = texMan.getTex(MAP_TEX_DIR + "asteroids", null);
    myBeaconAttackTex = texMan.getTex(MAP_TEX_DIR + "beaconAttack", null);
    myBeaconMoveTex = texMan.getTex(MAP_TEX_DIR + "beaconMove", null);
    myBeaconFollowTex = texMan.getTex(MAP_TEX_DIR + "beaconFollow", null);
    myZoom = MAX_ZOOM / MUL_FACTOR / MUL_FACTOR;
    myAreaWarnCol = new Color(Col.W);
    myAreaWarnBgCol = new Color(Col.UI_WARN);
    myWhiteTex = texMan.getTex(MAP_TEX_DIR + "whiteTex", null);
  }

  public boolean isToggled() {
    return myToggled;
  }

  public void draw(GameDrawer drawer, SolGame game) {
    SolCam cam = game.getCam();
    float iconSz = getIconRadius(cam) * 2;
    float starNodeW = cam.getViewHeight(myZoom) * STAR_NODE_SZ;
    float viewDist = cam.getViewDist(myZoom);
    FractionMan fractionMan = game.getFractionMan();
    SolShip hero = game.getHero();
    Planet np = game.getPlanetMan().getNearestPlanet();
    Vector2 camPos = cam.getPos();
    float heroDmgCap = hero == null ? Float.MAX_VALUE : HardnessCalc.getShipDmgCap(hero);

    drawer.updateMtx(game);
    game.getGridDrawer().draw(drawer, game, GRID_SZ);
    drawPlanets(drawer, game, viewDist, np, camPos, heroDmgCap);
    drawMazes(drawer, game, viewDist, np, camPos, heroDmgCap);
    drawStarNodes(drawer, game, viewDist, camPos, starNodeW);
    drawIcons(drawer, game, iconSz, viewDist, fractionMan, hero, camPos, heroDmgCap);
  }

  public float getIconRadius(SolCam cam) {
    return cam.getViewHeight(myZoom) * ICON_RAD;
  }

  private void drawMazes(GameDrawer drawer, SolGame game, float viewDist, Planet np, Vector2 camPos, float heroDmgCap) {
    ArrayList<Maze> mazes = game.getPlanetMan().getMazes();
    for (Maze maze : mazes) {
      Vector2 mazePos = maze.getPos();
      float rad = maze.getRadius() - MazeBuilder.BORDER;
      if (viewDist < camPos.dst(mazePos) - rad) continue;
      drawer.draw(myMazeTex, 2 * rad, 2 * rad, rad, rad, mazePos.x, mazePos.y, 45, Col.W);
      if (HardnessCalc.isDangerous(heroDmgCap, maze.getDps())) {
        drawAreaDanger(drawer, rad, mazePos, .75f);
      }
    }

  }

  private void drawPlanets(GameDrawer drawer, SolGame game, float viewDist, Planet np, Vector2 camPos, float heroDmgCap) {
    ArrayList<SolSystem> systems = game.getPlanetMan().getSystems();
    for (SolSystem sys : systems) {
      drawer.drawCircle(myWhiteTex, sys.getPos(), sys.getRadius(), Col.UI_MED, game.getCam().getRealLineWidth());
    }
    for (SolSystem sys : systems) {
      float dangerRad = HardnessCalc.isDangerous(heroDmgCap, sys.getDps()) ? sys.getRadius() : 0;
      Vector2 sysPos = sys.getPos();
      float rad = Const.SUN_RADIUS;
      if (camPos.dst(sysPos) - rad < viewDist) {
        drawer.draw(myStarTex, 2 * rad, 2 * rad, rad, rad, sysPos.x, sysPos.y, 0, Col.W);
      }

      Vector2 beltIconPos = SolMath.getVec();
      for (SystemBelt belt : sys.getBelts()) {
        float beltRad = belt.getRadius();
        float halfWidth = belt.getHalfWidth();
        int beltIconCount = (int) (.12f * beltRad);
        for (int i = 0; i < beltIconCount; i++) {
          float angle = 360f * i / beltIconCount;
          SolMath.fromAl(beltIconPos, angle, beltRad);
          beltIconPos.add(sysPos);
          drawer.draw(myBeltTex, 2 * halfWidth, 2 * halfWidth, halfWidth, halfWidth, beltIconPos.x, beltIconPos.y, angle * 3, Col.W);
        }
        float outerRad = beltRad + halfWidth;
        if (dangerRad < outerRad && HardnessCalc.isDangerous(heroDmgCap, belt.getDps())) dangerRad = outerRad;
      }
      SolMath.free(beltIconPos);
      if (dangerRad < sys.getInnerRad() && HardnessCalc.isDangerous(heroDmgCap, sys.getInnerDps())) dangerRad = sys.getInnerRad();
      if (dangerRad > 0) {
        drawAreaDanger(drawer, dangerRad, sysPos, .5f);
      }
    }

    for (Planet planet : game.getPlanetMan().getPlanets()) {
      Vector2 planetPos = planet.getPos();
      float fh = planet.getFullHeight();
      float dstToPlanetAtm = camPos.dst(planetPos) - fh;
      if (viewDist < dstToPlanetAtm) continue;
      drawer.draw(myAtmTex, 2*fh, 2*fh, fh, fh, planetPos.x, planetPos.y, 0, Col.UI_DARK);
      float gh;
      if (dstToPlanetAtm < 0) {
        gh = planet.getMinGroundHeight();
        drawer.draw(myPlanetCoreTex, 2*gh, 2*gh, gh, gh, planetPos.x, planetPos.y, planet.getAngle(), Col.W);
        drawNpGround(drawer, game, viewDist, np, camPos);
      } else {
        gh = planet.getGroundHeight();
        drawer.draw(myPlanetTex, 2*gh, 2*gh, gh, gh, planetPos.x, planetPos.y, planet.getAngle(), Col.W);
      }
      float dangerRad = HardnessCalc.isDangerous(heroDmgCap, planet.getAtmDps()) ? fh : 0;
      if (dangerRad < gh && HardnessCalc.isDangerous(heroDmgCap, planet.getGroundDps())) dangerRad = gh;
      if (dangerRad > 0) {
        drawAreaDanger(drawer, dangerRad, planetPos, .75f);
      }
    }
  }

  private void drawAreaDanger(GameDrawer drawer, float rad, Vector2 pos, float transpMul) {
    float perc = 2 * myAreaSkullTime / MAX_AREA_SKULL_TIME;
    if (perc > 1) perc = 2 - perc;
    float a = SolMath.clamp(perc * transpMul);
    myAreaWarnBgCol.a = a;
    myAreaWarnCol.a = a;
    drawer.draw(myWarnAreaBg, rad *2, rad *2, rad, rad, pos.x, pos.y, 0, myAreaWarnBgCol);
    rad *= INNER_ICON_PERC;
    drawer.draw(mySkullBigTex, rad *2, rad *2, rad, rad, pos.x, pos.y, 0, myAreaWarnCol);
  }

  private void drawIcons(GameDrawer drawer, SolGame game, float iconSz, float viewDist, FractionMan fractionMan,
    SolShip hero, Vector2 camPos, float heroDmgCap)
  {

    for (SolObj o : game.getObjMan().getObjs()) {
      Vector2 oPos = o.getPos();
      if (viewDist < camPos.dst(oPos)) continue;
      if ((o instanceof SolShip)) {
        SolShip ship = (SolShip) o;
        String hint = ship.getPilot().getMapHint();
        if (hint == null && !DebugOptions.DETAILED_MAP) continue;
        drawObjIcon(iconSz, oPos, ship.getAngle(), fractionMan, hero, ship.getPilot().getFraction(), heroDmgCap, o, ship.getHull().config.icon, drawer);
      }
      if ((o instanceof StarPort)) {
        StarPort sp = (StarPort) o;
        drawStarPortIcon(drawer, iconSz, sp.getFrom(), sp.getTo());
      }
    }

    for (FarObjData fod : game.getObjMan().getFarObjs()) {
      FarObj o = fod.fo;
      Vector2 oPos = o.getPos();
      if (viewDist < camPos.dst(oPos)) continue;
      if ((o instanceof FarShip)) {
        FarShip ship = (FarShip) o;
        String hint = ship.getPilot().getMapHint();
        if (hint == null && !DebugOptions.DETAILED_MAP) continue;
        drawObjIcon(iconSz, oPos, ship.getAngle(), fractionMan, hero, ship.getPilot().getFraction(), heroDmgCap, o, ship.getHullConfig().icon, drawer);
      }
      if ((o instanceof StarPort.MyFar)) {
        StarPort.MyFar sp = (StarPort.MyFar) o;
        drawStarPortIcon(drawer, iconSz, sp.getFrom(), sp.getTo());
      }
    }
    BeaconHandler bh = game.getBeaconHandler();
    BeaconHandler.Action bhAction = bh.getCurrAction();
    if (bhAction != null) {
      Vector2 beaconPos = bh.getPos();
      TextureRegion icon = myBeaconMoveTex;
      if (bhAction == BeaconHandler.Action.ATTACK) icon = myBeaconAttackTex;
      else if (bhAction == BeaconHandler.Action.FOLLOW) icon = myBeaconFollowTex;
      float beaconSz = iconSz * 1.5f;
      drawer.draw(icon, beaconSz, beaconSz, beaconSz/2, beaconSz/2, beaconPos.x, beaconPos.y, 0, Col.W);
    }
  }

  public void drawStarPortIcon(GameDrawer drawer, float iconSz, Planet from, Planet to) {
    float angle = SolMath.angle(from.getPos(), to.getPos());
    Vector2 pos = StarPort.getDesiredPos(from, to, false);
    drawObjIcon(iconSz, pos, angle, null, null, null, -1, null, myStarPortTex, drawer);
    SolMath.free(pos);
  }

  private void drawStarNodes(GameDrawer drawer, SolGame game, float viewDist, Vector2 camPos, float starNodeW)
  {
    for (SolObj o : game.getObjMan().getObjs()) {
      if (!(o instanceof StarPort)) continue;
      Vector2 oPos = o.getPos();
      if (viewDist < camPos.dst(oPos)) continue;
      StarPort sp = (StarPort) o;
      drawStarNode(drawer, sp.getFrom(), sp.getTo(), starNodeW);
    }

    for (FarObjData fod : game.getObjMan().getFarObjs()) {
      FarObj o = fod.fo;
      if (!(o instanceof StarPort.MyFar)) continue;
      Vector2 oPos = o.getPos();
      if (viewDist < camPos.dst(oPos)) continue;
      StarPort.MyFar sp = (StarPort.MyFar) o;
      if (!sp.isSecondary()) drawStarNode(drawer, sp.getFrom(), sp.getTo(), starNodeW);
    }
  }

  private void drawStarNode(GameDrawer drawer, Planet from, Planet to, float starNodeW) {
    Vector2 pos1 = StarPort.getDesiredPos(from, to, false);
    Vector2 pos2 = StarPort.getDesiredPos(to, from, false);
    drawer.drawLine(myWhiteTex, pos1, pos2, Col.UI_LIGHT, starNodeW);
    SolMath.free(pos1);
    SolMath.free(pos2);
  }

  private void drawNpGround(GameDrawer drawer, SolGame game, float viewDist, Planet np, Vector2 camPos) {
    ObjMan objMan = game.getObjMan();
    for (SolObj o : objMan.getObjs()) {
      if (!(o instanceof TileObj)) continue;
      TileObj to = (TileObj) o;
      if (to.getPlanet() != np) continue;
      Vector2 oPos = o.getPos();
      if (viewDist < camPos.dst(oPos)) continue;
      float sz = to.getSz();
      drawPlanetTile(to.getTile(), sz, drawer, myWhiteTex, oPos, to.getAngle());
    }

    for (FarObjData fod : objMan.getFarObjs()) {
      FarObj o = fod.fo;
      if (!(o instanceof FarTileObj)) continue;
      FarTileObj to = (FarTileObj) o;
      if (to.getPlanet() != np) continue;
      Vector2 oPos = o.getPos();
      if (viewDist < camPos.dst(oPos)) continue;
      float sz = to.getSz();
      drawPlanetTile(to.getTile(), sz, drawer, myWhiteTex, oPos, to.getAngle());
    }
  }

  public void drawObjIcon(float iconSz, Vector2 pos, float objAngle,
    FractionMan fractionMan, SolShip hero, Fraction objFrac, float heroDmgCap,
    Object shipHack, TextureAtlas.AtlasRegion icon, Object drawerHack)
  {
    boolean enemy = hero != null && fractionMan.areEnemies(objFrac, hero.getPilot().getFraction());
    float angle = objAngle;
    if (enemy && mySkullTime > 0 && HardnessCalc.isDangerous(heroDmgCap, shipHack)) {
      icon = mySkullTex;
      angle = 0;
    }
    float innerIconSz = iconSz * INNER_ICON_PERC;

    if (drawerHack instanceof UiDrawer) {
      UiDrawer uiDrawer = (UiDrawer) drawerHack;
      uiDrawer.draw(myIconBg, iconSz, iconSz, iconSz/2, iconSz/2, pos.x, pos.y, 0, enemy ? Col.UI_WARN : Col.UI_LIGHT);
      uiDrawer.draw(icon, innerIconSz, innerIconSz, innerIconSz/2, innerIconSz/2, pos.x, pos.y, angle, Col.W);
    } else {
      GameDrawer gameDrawer = (GameDrawer) drawerHack;
      gameDrawer.draw(myIconBg, iconSz, iconSz, iconSz/2, iconSz/2, pos.x, pos.y, 0, enemy ? Col.UI_WARN : Col.UI_LIGHT);
      gameDrawer.draw(icon, innerIconSz, innerIconSz, innerIconSz/2, innerIconSz/2, pos.x, pos.y, angle, Col.W);
    }
  }

  public void setToggled(boolean toggled) {
    myToggled = toggled;
  }

  public void changeZoom(boolean zoomIn) {
    if (zoomIn) myZoom /= MUL_FACTOR; else myZoom *= MUL_FACTOR;
    myZoom = SolMath.clamp(myZoom, MIN_ZOOM, MAX_ZOOM);
  }

  public float getZoom() {
    return myZoom;
  }

  public void update(SolGame game) {
    mySkullTime += game.getTimeStep();
    if (mySkullTime > MAX_SKULL_TIME) mySkullTime = -MAX_SKULL_TIME;
    myAreaSkullTime += game.getTimeStep();
    if (myAreaSkullTime > MAX_AREA_SKULL_TIME) myAreaSkullTime = 0;
  }

  private void drawPlanetTile(Tile t, float sz, GameDrawer drawer, TextureAtlas.AtlasRegion wt, Vector2 p, float angle) {
    float szh = .6f * sz;
    Color col = t.from == SurfDir.UP && t.to == SurfDir.UP ? Col.W : Col.UI_GROUND;
    if (t.from == SurfDir.FWD || t.from == SurfDir.UP) {
      if (t.from == SurfDir.UP) drawer.draw(wt, szh, szh, 0, 0, p.x, p.y, angle - 90, col);
      drawer.draw(wt, szh, szh, 0, 0, p.x, p.y, angle, col);
    }
    if (t.to == SurfDir.FWD || t.to == SurfDir.UP) {
      if (t.to == SurfDir.UP) drawer.draw(wt, szh, szh, 0, 0, p.x, p.y, angle + 180, col);
      drawer.draw(wt, szh, szh, 0, 0, p.x, p.y, angle + 90, col);
    }
  }

  public TextureAtlas.AtlasRegion getStarPortTex() {
    return myStarPortTex;
  }

}
