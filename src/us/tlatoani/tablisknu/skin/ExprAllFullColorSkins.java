package us.tlatoani.tablisknu.skin;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class ExprAllFullColorSkins extends SimpleExpression<Skin> {
    private static final Skin[] value = FullColorSkins.getSkinMap().values().toArray(new Skin[0]);

    @Override
    protected Skin[] get(Event e) {
        return value;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Skin> getReturnType() {
        return Skin.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "all full color skins";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
