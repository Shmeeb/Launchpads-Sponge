package pw.codehusky.launchpad;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

/**
 * Created by lokio on 12/19/2016.
 */
@Plugin(id="launchpad", name="Launchpad", version = "1.0-SNAPSHOT", description = "Launches people into the air :)")
public class Launchpad {
    @Inject
    private Logger logger;

    @Inject
    private PluginContainer pC;
    private Cause genericCause;
    @Listener
    public void gameStarted(GameStartedServerEvent event){
        logger.info("started c:");
        genericCause = Cause.of(NamedCause.of("PluginContainer",pC));
    }

    @Listener
    public void blockChange(ChangeBlockEvent.Modify event){
        if(event.getCause().root() instanceof Entity){
            Entity stepper = (Entity)event.getCause().root();
            BlockSnapshot fin = event.getTransactions().get(0).getFinal();
            BlockState bs = fin.getState();
            if(bs.getType() == BlockTypes.STONE_PRESSURE_PLATE || bs.getType() == BlockTypes.WOODEN_PRESSURE_PLATE){
                if(fin.getLocation().get().sub(0,1,0).getBlock().getType() == BlockTypes.LIT_REDSTONE_LAMP){
                    stepper.setVelocity(stepper.getVelocity().mul(6,0,6).add(0,1.5,0));
                }/*else if(stepper instanceof Player){
                    Player step = (Player) stepper;
                    ItemStack stack = ItemStack.of(ItemTypes.STONE_PRESSURE_PLATE,1);
                    stack.offer(Keys.DISPLAY_NAME, Text.of("Stepper"));
                    step.getInventory().offer(stack);
                }*/
            }


        }
    }
    @Listener
    public void blockPlace(ChangeBlockEvent.Place event){
        if(event.getCause().root() instanceof Player){
            Player cause = (Player) event.getCause().root();
            if(cause.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
                ItemStack inhand = cause.getItemInHand(HandTypes.MAIN_HAND).get();
                if(inhand.getItem() == ItemTypes.STONE_PRESSURE_PLATE){
                    if(inhand.get(Keys.DISPLAY_NAME).isPresent()){
                        if(inhand.get(Keys.DISPLAY_NAME).get().equals(Text.of("Stepper"))){
                            event.getTransactions().get(0).getFinal().getLocation().get().sub(0,1,0).setBlock(BlockState.builder().blockType(BlockTypes.REDSTONE_LAMP).build(),genericCause);
                        }
                    }
                }
            }
        }
    }

}
