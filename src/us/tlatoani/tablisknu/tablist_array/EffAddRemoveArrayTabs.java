package us.tlatoani.tablisknu.tablist_array;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import us.tlatoani.tablisknu.skin.Skin;
import us.tlatoani.tablisknu.tablist.ArrayTablist;
import us.tlatoani.tablisknu.tablist.TablistProvider;
import us.tlatoani.tablisknu.tablist.Tablist;
import org.bukkit.event.Event;
import us.tlatoani.tablisknu.tablist_general.TablistMundo;

import java.util.Optional;

/**
 * Created by Tlatoani on 6/16/18.
 */
public class EffAddRemoveArrayTabs extends Effect {
    private TablistProvider tablistProvider;
    private boolean add;
    private boolean isColumns;
    private Optional<Expression<Number>> addendExpression;
    private Optional<Expression<Skin>> iconExpression;

    @Override
    protected void execute(Event event) {
        Integer addend = addendExpression
                .map(expr -> Optional.ofNullable(expr.getSingle(event)))
                .orElse(Optional.ofNullable(1))
                .map(Number::intValue)
                .map(num -> num * (add ? 1 : -1))
                .orElse(null);
        Skin icon = iconExpression.map(expr -> expr.getSingle(event)).orElse(null);
        if (addend != null) {
            for (Tablist tablist : tablistProvider.get(event)) {
                if (tablist.getSupplementaryTablist() instanceof ArrayTablist) {
                    ArrayTablist arrayTablist = (ArrayTablist) tablist.getSupplementaryTablist();
                    if (isColumns) {
                        arrayTablist.setColumns(arrayTablist.getColumns() + addend, icon);
                    } else {
                        arrayTablist.setRows(arrayTablist.getRows() + addend, icon);
                    }
                }
            }
        }
    }

    @Override
    public String toString(Event event, boolean b) {
        return tablistProvider.toString((add ? "add " : "remove ")
                + addendExpression.map(Object::toString).orElse("a")
                + " "
                + (isColumns ? "column" : "row")
                + (addendExpression.isPresent() ? "s" : "")
                + iconExpression.map(expr -> " with icon " + expr).orElse("")
                + (add ? " to" : " from") + " array tablist [of %]");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        add = i < 2;
        isColumns = parseResult.mark == 0;
        addendExpression = Optional.ofNullable((Expression<Number>) expressions[0]);
        iconExpression = add ? Optional.ofNullable((Expression<Skin>) expressions[1]) : Optional.empty();
        tablistProvider = TablistProvider.of(expressions, add ? 2 : 1);
        TablistMundo.printTablistSyntaxWarning(
                "Using tablist dimensions other than 4 rows and 20 columns",
                "use the default tablist dimensions of 4 rows and 20 columns");
        return true;
    }
}
