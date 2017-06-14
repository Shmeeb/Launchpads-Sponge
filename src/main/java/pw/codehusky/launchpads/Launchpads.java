package pw.codehusky.launchpads;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by lokio on 12/19/2016.
 */
@Plugin(id="launchpads", name="Launchpads", version = "1.1.0", description = "Launches people into the air :)")
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
        logger = LoggerFactory.getLogger(pC.getName());
        logger.info("started c:");
        genericCause = Cause.of(NamedCause.of("PluginContainer",pC));
        Sponge.getScheduler().createTaskBuilder().async().execute(new Consumer<Task>() {
            @Override
            public void accept(Task task) {
                try {
                    JSONObject obj = pw.codehusky.launchpads.JsonReader.readJsonFromUrl("https://api.github.com/repos/codehusky/Launchpads-Sponge/releases");
                    String[] thisVersion = pC.getVersion().get().split("\\.");
                    String[] remoteVersion = obj.getJSONArray("releases").getJSONObject(0).getString("tag_name").replace("v","").split("\\.");
                    for(int i = 0; i < Math.min(remoteVersion.length,thisVersion.length); i++){
                        if(!thisVersion[i].equals(remoteVersion[i])){
                            if(Integer.parseInt(thisVersion[i]) > Integer.parseInt(remoteVersion[i])){
                                //we're ahead
                                logger.warn("----------------------------------------------------");
                                logger.warn("Running unreleased version. (Developer build?)");
                                logger.warn("----------------------------------------------------");
                            }else{
                                //we're behind
                                logger.warn("----------------------------------------------------");
                                logger.warn("Your version of Launchpads is out of date!");
                                logger.warn("Your version: v" + pC.getVersion().get());
                                logger.warn("Latest version: " + obj.getJSONArray("releases").getJSONObject(0).getString("tag_name"));
                                logger.warn("Update here: https://goo.gl/ZUTX03");
                                logger.warn("----------------------------------------------------");
                            }
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).submit(this);

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
