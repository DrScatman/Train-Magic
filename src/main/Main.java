package main;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(name = "Splash", desc = "Just Splash", developer = "DrScatman")
public class Main extends Script implements ChatMessageListener {

    private static final int STOP_LVL = 13;
    private static final String NPC = "Theif";
    private static final Area SPLASH_AREA_MUGGER = Area.rectangular(3010, 3187, 3020, 3182);
    private static final Area SPLASH_AREA_THIEF = Area.rectangular(3010, 3196, 3016, 3190);    private static final Spell spell = Spell.Modern.WIND_STRIKE;
    private boolean shiftPosition;
    private int lvl;

    @Override
    public int loop() {
        if (!SPLASH_AREA_THIEF.contains(Players.getLocal())) {
            Movement.walkToRandomized(SPLASH_AREA_THIEF.getCenter());
        }

        if (Dialog.canContinue()) {
            Dialog.processContinue();
            if (Skills.getLevel(Skill.MAGIC) != lvl) {
                lvl = Skills.getLevel(Skill.MAGIC);
                Log.fine("Magic LVL: " + lvl);
            }
            if (lvl >= STOP_LVL) {
                this.setStopping(true);
            }
        }

        if (!Magic.Autocast.isEnabled() || !Magic.Autocast.isSpellSelected(spell)) {
            Log.info("Setting autocast");
            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, spell);
        }

        Npc npc = Npcs.getNearest(NPC);
        if (npc != null && Players.getLocal().getTargetIndex() == -1 && !Players.getLocal().isAnimating()) {
            Log.info("Manual cast");
            if (!Magic.cast(spell, npc) || shiftPosition) {
                Log.info("Shifting position");
                Movement.walkTo(Players.getLocal().getPosition().translate(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
                shiftPosition = false;
            }
        } else if (npc == null){
            Log.severe("Cant Find Npc");
        }

        return Random.high(2000, 5000);
    }

    @Override
    public void notify(ChatMessageEvent e) {
        if (e.getType().equals(ChatMessageType.PUBLIC) || e.getType().equals(ChatMessageType.PRIVATE_RECEIVED))
            return;
        if (e.getMessage().toLowerCase().contains("reach that!")) {
            shiftPosition = true;
        }
    }
}
