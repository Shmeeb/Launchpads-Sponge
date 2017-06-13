package pw.codehusky.launchpads;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.HashMap;

/**
 * Created by lokio on 12/19/2016.
 */
@Plugin(id="launchpads", name="Launchpads", version = "1.0", description = "Launches people into the air :)")
public class Launchpads {
    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> privateConfig;

    @Inject
    private PluginContainer pC;
    private Cause genericCause;

    private HashMap<BlockType,Double> launchpadTypes;

    @Listener
    public void gameStarted(GameStartedServerEvent event){
        logger.info("started c:");
        genericCause = Cause.of(NamedCause.of("PluginContainer",pC));

    }

    public double getLaunchpadPower(BlockType launchpadType){
        launchpadTypes = new HashMap<>();
        launchpadTypes.put(BlockTypes.COAL_BLOCK,3d);
        launchpadTypes.put(BlockTypes.IRON_BLOCK,4d);
        launchpadTypes.put(BlockTypes.GOLD_BLOCK,5d);
        launchpadTypes.put(BlockTypes.REDSTONE_BLOCK,6d);
        launchpadTypes.put(BlockTypes.DIAMOND_BLOCK,7d);
        launchpadTypes.put(BlockTypes.EMERALD_BLOCK,8d);
        if(!launchpadTypes.containsKey(launchpadType))
            return -1;
        return launchpadTypes.get(launchpadType);
    }
    @Listener
    public void blockChange(ChangeBlockEvent.Modify event){
        if(event.getCause().root() instanceof Entity){
            Entity stepper = (Entity)event.getCause().root();
            BlockSnapshot fin = event.getTransactions().get(0).getFinal();
            BlockState bs = fin.getState();
            if(bs.getType() == BlockTypes.STONE_PRESSURE_PLATE || bs.getType() == BlockTypes.WOODEN_PRESSURE_PLATE){
                BlockType under = fin.getLocation().get().sub(0,2,0).getBlock().getType();
                double gg = getLaunchpadPower(under);
                if(gg == -1){
                    gg = getLaunchpadPower(fin.getLocation().get().sub(0,1,0).getBlock().getType());
                }
                if(gg != -1) {
                    stepper.setVelocity(stepper.getVelocity().mul(gg, 0, gg).add(0, gg / 4, 0));
                    event.setCancelled(true);
                }

            }


        }
    }

}
