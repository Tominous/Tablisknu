package us.tlatoani.tablisknu.tablist_array;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import us.tlatoani.tablisknu.skin.Skin;
import us.tlatoani.tablisknu.tablist.ArrayTablist;
import us.tlatoani.tablisknu.tablist.PlayerTablist;
import us.tlatoani.tablisknu.tablist.SimpleTablist;
import us.tlatoani.tablisknu.tablist.Tablist;
import us.tlatoani.tablisknu.tablist.TablistProvider;
import org.bukkit.event.Event;
import us.tlatoani.tablisknu.tablist_general.TablistMundo;

import java.util.Optional;

/**
 * Created by Tlatoani on 7/13/16.
 */
public class EffEnableDisableArrayTablist extends Effect {
    private TablistProvider tablistProvider;
    private boolean enable;
    private Optional<Expression<Number>> columns;
    private Optional<Expression<Number>> rows;
    private Optional<Expression<Skin>> iconExpression;

    @Override
    protected void execute(Event event) {
        if (enable) {
            int columns = this.columns.map(expression -> expression.getSingle(event).intValue()).orElse(4);
            int rows = this.rows.map(expression -> expression.getSingle(event).intValue()).orElse(20);
            Skin initialIcon = this.iconExpression.map(expression -> expression.getSingle(event)).orElse(null);
            for (Tablist tablist : tablistProvider.get(event)) {
                tablist.setSupplementaryTablist(playerTablist -> {
                    if (initialIcon != null) {
                        tablist.setDefaultIcon(initialIcon);
                    }
                    return new ArrayTablist(playerTablist, columns, rows);
                });
            }
        } else {
            for (Tablist tablist : tablistProvider.get(event)) {
                if (tablist.getSupplementaryTablist() instanceof ArrayTablist) {
                    tablist.setSupplementaryTablist(SimpleTablist::new);
                    tablist.getPlayerTablist().ifPresent(PlayerTablist::showAllPlayers);
                }
            }
        }
    }

    @Override
    public String toString(Event event, boolean b) {
        return tablistProvider.toString((enable ? "enable" : "disable") + " array tablist [for %]" +
                ((enable && (columns.isPresent() || rows.isPresent() || iconExpression.isPresent())) ? " with"
                    + columns.map(expression -> " " + expression + " columns").orElse("")
                    + rows.map(expression -> " " + expression + " rows").orElse("")
                    + iconExpression.map(expression -> " initial icon " + expression).orElse("")
                : ""));
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        tablistProvider = TablistProvider.of(expressions, 0);
        enable = i == 0;
        if (enable) {
            columns = Optional.ofNullable((Expression<Number>) expressions[2]);
            rows = Optional.ofNullable((Expression<Number>) expressions[3]);
            iconExpression = Optional.ofNullable((Expression<Skin>) expressions[4]);
        }
        if (enable && (columns.isPresent() || rows.isPresent())) {
            TablistMundo.printTablistSyntaxWarning(
                    "Using tablist dimensions other than 4 rows and 20 columns",
                    "use the default tablist dimensions of 4 rows and 20 columns");
        }
        return true;
    }
}
