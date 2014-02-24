package com.miloshpetrov.sol2.game.ship;

import com.badlogic.gdx.math.Vector2;
import com.miloshpetrov.sol2.game.*;
import com.miloshpetrov.sol2.game.gun.GunItem;
import com.miloshpetrov.sol2.game.input.Pilot;
import com.miloshpetrov.sol2.game.item.*;

public class FarShip implements FarObj {
  private final Vector2 myPos;
  private final Vector2 mySpd;
  private final Shield myShield;
  private final Armor myArmor;
  private float myAngle;
  private final float myRotSpd;
  private final Pilot myPilot;
  private final ItemContainer myContainer;
  private final HullConfig myHullConfig;
  private float myLife;
  private final boolean myMount1Fixed;
  private final boolean myMount2Fixed;
  private final GunItem myGun1;
  private final GunItem myGun2;
  private final float myRadius;
  private final RemoveController myRemoveController;
  private final EngineItem myEngine;
  private ShipRepairer myRepairer;
  private float myMoney;
  private final ItemContainer myTradeContainer;

  public FarShip(Vector2 pos, Vector2 spd, float angle, float rotSpd, Pilot pilot, ItemContainer container,
    HullConfig hullConfig, float life, boolean mount1Fixed,
    boolean mount2Fixed, GunItem gun1, GunItem gun2, float radius, RemoveController removeController, EngineItem engine,
    ShipRepairer repairer, float money, ItemContainer tradeContainer, Shield shield, Armor armor)
  {
    myPos = pos;
    mySpd = spd;
    myAngle = angle;
    myRotSpd = rotSpd;
    myPilot = pilot;
    myContainer = container;
    myHullConfig = hullConfig;
    myLife = life;
    myMount1Fixed = mount1Fixed;
    myMount2Fixed = mount2Fixed;
    myGun1 = gun1;
    myGun2 = gun2;
    myRadius = radius;
    myRemoveController = removeController;
    myEngine = engine;
    myRepairer = repairer;
    myMoney = money;
    myTradeContainer = tradeContainer;
    myShield = shield;
    myArmor = armor;
  }

  @Override
  public boolean shouldBeRemoved(SolGame game) {
    return myRemoveController != null && myRemoveController.shouldRemove(myPos);
  }

  @Override
  public SolObj toObj(SolGame game) {
    return game.getShipBuilder().build(game, myPos, mySpd, myAngle, myRotSpd, myPilot, myContainer, myHullConfig, myLife, myMount1Fixed, myMount2Fixed, myGun1,
      myGun2, myRemoveController, myEngine, myRepairer, myMoney, myTradeContainer, myShield, myArmor);
  }

  @Override
  public void update(SolGame game) {
    myPilot.updateFar(game, this);
    game.getTradeMan().manage(game, myTradeContainer, myHullConfig);
    if (myRepairer != null) myLife += myRepairer.tryRepair(game, myContainer, myLife, myHullConfig);
  }

  @Override
  public float getRadius() {
    return myRadius;
  }

  @Override
  public Vector2 getPos() {
    return myPos;
  }

  @Override
  public String toDebugString() {
    return null;
  }

  public void setPos(Vector2 pos) {
    myPos.set(pos);
  }

  public void setSpd(Vector2 spd) {
    mySpd.set(spd);
  }

  public Pilot getPilot() {
    return myPilot;
  }

  public HullConfig getHullConfig() {
    return myHullConfig;
  }

  public float getAngle() {
    return myAngle;
  }

  public Vector2 getSpd() {
    return mySpd;
  }

  public EngineItem getEngine() {
    return myEngine;
  }

  public void setAngle(float angle) {
    myAngle = angle;
  }

  public GunItem getGun(boolean secondary) {
    return secondary ? myGun2 : myGun1;
  }

  public Shield getShield() {
    return myShield;
  }

  public Armor getArmor() {
    return myArmor;
  }

  public float getLife() {
    return myLife;
  }

  public boolean isMountFixed(boolean sec) {
    return sec ? myMount2Fixed : myMount1Fixed;
  }
}