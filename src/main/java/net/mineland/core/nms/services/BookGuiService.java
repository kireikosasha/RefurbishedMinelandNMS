package net.mineland.core.nms.services;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.mineland.core.nms.MinelandNMSPlugin;
import net.mineland.core.nms.api.MinelandNMS;
import net.mineland.core.nms.api.java.RobustBukkitLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

@UtilityClass
public class BookGuiService {
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    /*
    Билдер для книги и дальнейшего использования в GUI
     */
    public ItemStack createBook(final String title, final String author, final List<String> pages) {
        final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        final BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(title);
            meta.setAuthor(author);
            meta.setPages(pages);
            book.setItemMeta(meta);
        }
        return book;
    }

     /*
     Билдер для книги и дальнейшего
     использования в GUI с интеракцией...
     Например, книга с кнопками.
     */
     public ItemStack createInteractiveBook(final String title, final String author, final List<String> pages, final List<String> commands) {
         if (pages.size() != commands.size()) {
             throw new IllegalArgumentException("Pages and commands lists must be of the same size!");
         }
         final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
         final BookMeta meta = (BookMeta) book.getItemMeta();
         if (meta != null) {
             meta.setTitle(title);
             meta.setAuthor(author);
             for (int i = 0; i < pages.size(); i++) {
                 final String pageText = pages.get(i);
                 final String command = commands.get(i);
                 final ComponentBuilder pageBuilder = new ComponentBuilder(pageText);
                 if (command != null)
                     pageBuilder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                 meta.spigot().addPage(pageBuilder.create());
             }
             book.setItemMeta(meta);
         }
         return book;
     }

    // Открытие Book Gui
    public void openBookGui(final Player player, final ItemStack book) {
        if (book.getType() != Material.WRITTEN_BOOK)
            throw new IllegalArgumentException("ItemStack must be a written book!");
        /*
        По делам книг берется клиент,
        поэтому нужно приходить
        к таким костылям.
        Кстати фиг тебе, а не асинхронность
        (Как будто что она тут нужна).
         */
        Bukkit.getScheduler().runTask(MinelandNMSPlugin.getInstance(), () -> {
            final int slot = player.getInventory().getHeldItemSlot();
            final ItemStack old = player.getInventory().getItem(slot);
            player.getInventory().setItem(slot, book);
            try {
                if (protocolManager.getMinecraftVersion().compareTo(new MinecraftVersion("1.14")) >= 0) {
                    /*
                    Реализация 1.14+, она более
                    менее простая.
                     */
                    final PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.OPEN_BOOK);
                    packet.getEnumModifier(EnumWrappers.Hand.class, 0).write(0, EnumWrappers.Hand.MAIN_HAND);
                    protocolManager.sendServerPacket(player, packet);
                } else {
                    // Неприятный человек попался
                    final ByteBuf payload = Unpooled.buffer(1);
                    payload.writeByte(0);
                    MinelandNMS.sendCustomPayload(player, "MC|BOpen", payload);
                }
            } catch (final Exception e) {
                /*
                А вдруг код сломается?
                Не дадим предмету пропасть.
                 */
                RobustBukkitLogger.logException(e);
            } finally {
                // возвращаем предмет
                player.getInventory().setItem(slot, old);
            }
        });
    }
}
