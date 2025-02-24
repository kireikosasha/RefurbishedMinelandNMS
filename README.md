# Refurbished Mineland NMS
...являет собой полную перекодировку NMS для перехода на ProtocolLib 5.0.

80% NMS переписано на ProtocolLib и поддерживает так и 1.12, так и 1.16, поэтому это должно обеспечить спокойный переход на 5.0.

Большее внимание выделено основным проблемам: NPC и Book GUI, они выделены в отдельные комплексные сервисы, что обеспечит максимальное удобство и контроль.

Основные особенности:
- Улучшенный ExecutorScheduler для асинхронных пакетов (я рекомендую отправлять все асинхронные запросы через MinelandNMS.sendPacketAsync).
- Мультиверсия (и 1.12, и 1.16 - поддерживаются).
- Управление NPC через созданный экземпляр.
- Поддержка ProtocolLib 5.0+

**Нужно понимать, что он работает как отдельный API плагин!**


----------

**NPC Service** *(net/mineland/core/nms/services/NPCService.java)*

Создан для замены довольно геморройных методов, вот как тут всё просто:

`
NMSFakePlayer npc = NPCService.spawnNPC(toPlayer, location, npcName, skinValue, skinSignature);
`

Примеры дальнейшего управления:

`
npc.changeName(newName);
`

`
changeLocation(final Location newLocation);
`

`npc.hide(); ncp.show();`
`NPCService.delete(NMSFakePlayer);`


----------
**Book GUI** *(net/mineland/core/nms/services/BookGuiService.java)*

Тут всё ещё проще...

`openBookGui(Player player, ItemStack book)`

Так-же есть встроенные builder's для book ItemStack:

`createBook(String title, String author, List<String> pages)`

`createInteractiveBook(String title, String author, List<String> pages, List<String> commands)`



----------

**Остальной функционал**

Если возникла проблема не только с данными компонентами при переносе на Protocol 5.0, в классе
`MinelandNMS` представлен остальный функционал, переделанный на ProtocolLib.

**В BootService нужно выключить тестовые листенеры перед установкой на проду!**
