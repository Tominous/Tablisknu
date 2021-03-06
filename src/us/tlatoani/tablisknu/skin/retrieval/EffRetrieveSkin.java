package us.tlatoani.tablisknu.skin.retrieval;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import us.tlatoani.tablisknu.blueprint.Blueprint;
import us.tlatoani.tablisknu.skin.ProfileManager;
import us.tlatoani.tablisknu.skin.Skin;
import us.tlatoani.tablisknu.util.LocalVariablesRestorer;

import java.util.Optional;
import java.util.UUID;

public class EffRetrieveSkin extends Effect {
    private Expression<String> stringExpr;
    private Expression<OfflinePlayer> offlinePlayerExpr;
    private Expression<Blueprint> blueprintExpr;
    private Optional<Expression<Timespan>> timeoutExpr;
    private RetrieveMode mode;
    private Variable<?> variable;
    private SkinFormat format;

    public enum RetrieveMode {
        FILE, URL, UUID, OFFLINE_PLAYER, BLUEPRINT
    }

    @Override
    protected TriggerItem walk(Event event) {
        int timeoutMillis = timeoutExpr
                .map(expr -> expr.getSingle(event))
                .map(Timespan::getMilliSeconds)
                .map(Number::intValue)
                .orElse(10000);
        LocalVariablesRestorer localVariablesRestorer = new LocalVariablesRestorer(event);
        if (mode == RetrieveMode.FILE || mode == RetrieveMode.URL) {
            String path = stringExpr.getSingle(event);
            if (path == null) {
                return getNext();
            }
            localVariablesRestorer.removeVariables();
            MineSkinRetrieval.retrieveFromMineSkinAPI(
                    mode == RetrieveMode.FILE ? MineSkinRetrieval.Source.FILE : MineSkinRetrieval.Source.URL,
                    path,
                    format,
                    timeoutMillis,
                    skin -> afterRetrieval(event, skin, localVariablesRestorer)
            );
        } else if (mode == RetrieveMode.UUID || mode == RetrieveMode.OFFLINE_PLAYER) {
            OfflinePlayer offlinePlayer;
            if (mode == RetrieveMode.UUID) {
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(stringExpr.getSingle(event)));
            } else {
                offlinePlayer = offlinePlayerExpr.getSingle(event);
            }
            if (offlinePlayer == null) {
                return getNext();
            }
            if (Bukkit.getOnlineMode()) {
                if (offlinePlayer.isOnline()) {
                    Skin skin = ProfileManager.getProfile(offlinePlayer.getPlayer()).getActualSkin();
                    if (skin != null && !Skin.EMPTY.equals(skin)) {
                        variable.change(event, new Skin[]{skin}, Changer.ChangeMode.SET);
                        return getNext();
                    }
                }
                localVariablesRestorer.removeVariables();
                PlayerSkinRetrieval.retrieveSkinFromUUID(
                        offlinePlayer.getUniqueId(), timeoutMillis, skin -> afterRetrieval(event, skin, localVariablesRestorer));
            } else {
                localVariablesRestorer.removeVariables();
                PlayerSkinRetrieval.retrieveSkinFromName(
                        offlinePlayer.getName(), timeoutMillis, skin -> afterRetrieval(event, skin, localVariablesRestorer));
            }
        } else if (mode == RetrieveMode.BLUEPRINT) {
            Blueprint blueprint = blueprintExpr.getSingle(event);
            if (blueprint == null) {
                return getNext();
            }
            localVariablesRestorer.removeVariables();
            MineSkinRetrieval.retrieveFromMineSkinAPIUsingBlueprint(blueprint, timeoutMillis, skin -> afterRetrieval(event, skin, localVariablesRestorer));
        }
        return null;
    }

    private void afterRetrieval(Event event, Skin skin, LocalVariablesRestorer localVariablesRestorer) {
        localVariablesRestorer.restoreVariables();
        variable.change(event, new Skin[]{skin}, Changer.ChangeMode.SET);
        TriggerItem.walk(getNext(), event);
    }

    @Override
    protected void execute(Event event) {}

    @Override
    public String toString(Event event, boolean b) {
        String suffix = timeoutExpr.map(expr -> " with timeout " + expr).orElse("") + " into " + variable;
        switch (mode) {
            case FILE: return "retrieve" + (format == SkinFormat.STEVE ? "" : " slim") + " skin from file " + stringExpr + suffix;
            case URL: return "retrieve" + (format == SkinFormat.STEVE ? "" : " slim") + " skin from url " + stringExpr + suffix;
            case UUID: return "retrieve skin from uuid " + stringExpr + suffix;
            case OFFLINE_PLAYER: return "retrieve skin of " + offlinePlayerExpr + suffix;
        }
        throw new IllegalStateException("RetrieveMode = " + mode);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        mode = RetrieveMode.values()[parseResult.mark & 0b111];
        format = SkinFormat.values()[parseResult.mark >> 3];
        stringExpr = (Expression<String>) expressions[0];
        if (mode == RetrieveMode.OFFLINE_PLAYER) {
            offlinePlayerExpr = (Expression<OfflinePlayer>) expressions[1];
        } else if (mode == RetrieveMode.BLUEPRINT) {
            blueprintExpr = (Expression<Blueprint>) expressions[2];
        }
        timeoutExpr = Optional.ofNullable((Expression<Timespan>) expressions[expressions.length - 2]);
        if (!(expressions[expressions.length - 1] instanceof Variable)) {
            Skript.error("The 'retrieve skin' effect can only retrieve into variables!");
            return false;
        }
        variable = (Variable) expressions[expressions.length - 1];
        return true;
    }
}
