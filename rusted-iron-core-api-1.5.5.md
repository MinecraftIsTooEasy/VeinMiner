# RustedIronCore (锈铁核心) API 参考文档

> 适用版本: RIC 1.5.0+ | Minecraft 1.6.4-MITE | FishModLoader 3.4.2+

---

## 目录

1. [事件系统 (Event System)](#1-事件系统-event-system)
   - 1.1 [Handlers 注册表](#11-handlers-注册表)
   - 1.2 [CombatHandler — 战斗/伤害](#12-combathandler--战斗伤害)
   - 1.3 [EntityEventHandler — 实体生命周期](#13-entityeventhandler--实体生命周期)
   - 1.4 [PlayerEventHandler — 玩家事件](#14-playereventhandler--玩家事件)
   - 1.5 [TickHandler — Tick 循环](#15-tickhandler--tick-循环)
   - 1.6 [PlayerAttributeHandler — 玩家属性](#16-playerattributehandler--玩家属性)
   - 1.7 [其他 Handler](#17-其他-handler)
   - 1.8 [事件对象 (Events)](#18-事件对象-events)
   - 1.9 [事件系统底层机制](#19-事件系统底层机制)
2. [注册系统 (Registry)](#2-注册系统-registry)
   - 2.1 [MinecraftRegistry — 方块/物品注册](#21-minecraftregistry--方块物品注册)
   - 2.2 [IGameRegistry — 入口点](#22-igameregistry--入口点)
   - 2.3 [属性注册 (Property System)](#23-属性注册-property-system)
3. [方块与物品基类 (Blocks & Items)](#3-方块与物品基类)
   - 3.1 [方块基类 (api/block)](#31-方块基类-apiblock)
   - 3.2 [物品基类 (api/item)](#32-物品基类-apiitem)
   - 3.3 [材料接口 (extension/material)](#33-材料接口-extensionmaterial)
   - 3.4 [物品扩展接口 (extension/item)](#34-物品扩展接口-extensionitem)
4. [网络系统 (Network)](#4-网络系统-network)
5. [工具类 (Utilities)](#5-工具类)
   - 5.1 [Accessor — 反射工具](#51-accessor--反射工具)
   - 5.2 [IdUtilExtra — ID 分配](#52-idutilextra--id-分配)
   - 5.3 [FabricUtil — 模组元数据](#53-fabricutil--模组元数据)
   - 5.4 [RandomUtil — 加权随机](#54-randomutil--加权随机)
   - 5.5 [其他工具](#55-其他工具)
6. [玩家 API (Player API)](#6-玩家-api)
7. [世界/维度 API (World & Dimension)](#7-世界维度-api)
8. [模型与渲染 API (Model & Render)](#8-模型与渲染-api)
9. [村民交易 API (Villager Trading)](#9-村民交易-api)
10. [键位绑定与本地化 (Keybinding & I18n)](#10-键位绑定与本地化)
11. [常用 Mixin 接口 (Common Mixins)](#11-常用-mixin-接口)
12. [附录 (Appendix)](#12-附录-appendix)
    - 12.1 [API 快速查找表](#附录-aapi-快速查找表)
        - [事件系统 (Event System)](#a1-事件系统-event-system)
        - [注册与配方 (Registry & Recipes)](#a2-注册与配方-registry--recipes)
        - [网络通信 (Network)](#a3-网络通信-network)
        - [工具与辅助 (Utilities)](#a4-工具与辅助-utilities)
    - 12.2 [环境注解 (Environment)](#附录-b环境注解-environment)

---

## 1. 事件系统 (Event System)

### 设计模式

```
Handler          -> 管理 Listener 列表，批量调用
EventHandler<T>  -> 带 pre/listeners/post 三阶段 + 一次性发布
StagedHandler    -> 仅初始化阶段发布的 Runnable 列表
```

### 注册入口

```java
// 模组必须通过这个类注册所有 Listener
moddedmite.rustedironcore.api.event.Handlers
```

`Handlers` 是一个静态字段集合，包含所有 Handler 实例。模组在 `ModInitializer.onInitialize()` 或 `IInitializationListener` 中注册：

```java
Handlers.Combat.register(new ICombatListener() { ... });
Handlers.EntityEvent.register(new IEntityEventListener() { ... });
```

---

### 1.1 Handlers 注册表

所有 Handler 静态字段（在 `moddedmite.rustedironcore.api.event.Handlers` 中）：

| 字段 | 类型 | 用途 |
|---|---|---|
| `Combat` | `CombatHandler` | 战斗/伤害修改 |
| `EntityEvent` | `EntityEventHandler` | 实体生命周期 |
| `EntityMobMixin` | `EntityMobMixinHandler` | EntityMob NBT 读写 |
| `PlayerEvent` | `PlayerEventHandler` (SERVER) | 玩家登录/登出/重生 |
| `PlayerAttribute` | `PlayerAttributeHandler` | 玩家属性上限修改 |
| `Tick` | `TickHandler` | Tick 循环 |
| `Achievement` | `AchievementHandler` | 成就触发 |
| `Crafting` | `CraftingHandler` | 合成配方注册 |
| `Smelting` | `SmeltingHandler` | 熔炼配方注册 |
| `FurnaceUpdate` | `FurnaceUpdateHandler` | 熔炉更新 |
| `Barbecue` | `BarbecueHandler` | 烧烤逻辑 |
| `GravelDrop` | `GravelDropHandler` | 砾石掉落 |
| `LootTable` | `LootTableHandler` | 战利品表 |
| `Enchanting` | `EnchantingHandler` | 附魔修改 |
| `Initialization` | `InitializationHandler` | 客户端/服务端启动 |
| `Connection` | `ConnectionHandler` | 网络连接 |
| `Dimension` | `DimensionHandler` | 维度注册 |
| `EntityTracker` | `EntityTrackerHandler` | 实体追踪器 |
| `TileEntityData` | `TileEntityDataHandler` | TileEntity 类型 |
| `SpawnCondition` | `SpawnConditionHandler` | 实体生成条件 |
| `OreGeneration` | `OreGenerationHandler` | 矿石生成 |
| `BiomeGenerate` | `BiomeGenerateHandler` | 生物群系生成 |
| `BiomeDecoration` | `BiomeDecorationHandler` | 群系装饰 |
| `MapGen` | `MapGenHandler` | 地图结构生成 |
| `StructureNBT` | `StructureNBTHandler` | 结构 NBT |
| `Trading` | `TradingHandler` | 村民职业/交易 |
| `TimedTask` | `TimedTaskHandler` | 定时任务 |
| `WorldInfo` | `WorldInfoHandler` | level.dat 读写 |
| `WorldLoad` | `WorldLoadHandler` (CLIENT) | 客户端世界加载 |
| `ChunkLoad` | `ChunkLoadHandler` | 区块加载/卸载 |
| `Screen` | `ScreenHandler` | GUI 生命周期 |
| `Tooltip` | `TooltipHandler` (CLIENT) | 物品提示信息 |
| `ArmorModel` | `ArmorModelHandler` (CLIENT) | 护甲模型 |
| `Keybinding` | `KeybindingHandler` (CLIENT) | 按键绑定注册 |
| `JsonModel` | `JsonModelHandler` (CLIENT) | JSON 模型注册 |
| `ArrowRegister` | `ArrowRegisterHandler` | 箭矢材料 |
| `BeaconUpdate` | `BeaconUpdateHandler` | 信标更新 |
| `Command` | `CommandHandler` | 命令注册 |
| `PropertiesRegistry` | `AbstractHandler<Runnable>` | 属性注册阶段 |
| `PotionRegistry` | `AbstractHandler<Runnable>` | 药水注册阶段 |
| `Structure` | `StructureHandler` (deprecated) | 结构注册 (弃用, 用 MapGen 替代) |

---

### 1.2 CombatHandler — 战斗/伤害

**Listener 接口**: `ICombatListener`
**全限定名**: `moddedmite.rustedironcore.api.event.listener.ICombatListener`

```java
public interface ICombatListener {
    // 修改玩家受到的 Damage 对象（forEach 遍历所有 listener）
    void onPlayerReceiveDamageModify(EntityPlayer player, Damage damage);

    // 修改怪物受到的 Damage 对象
    void onMobReceiveDamageModify(EntityMob mob, Damage damage);

    // 链式修改护甲保护值（返回值作为下一个 listener 的输入）
    float onArmorProtectionModify(ItemStack item_stack, EntityLivingBase owner, float original);

    // 链式修改玩家近战原始伤害
    float onPlayerRawMeleeDamageModify(EntityPlayer player, Entity target,
            boolean critical, boolean suspended_in_liquid, float original);

    // 链式修改玩家方块触及距离
    float onPlayerBlockReachModify(EntityPlayer player, Block block, int metadata, float original);

    // 链式修改玩家实体触及距离
    float onPlayerEntityReachModify(EntityPlayer player, EnumEntityReachContext context,
            Entity entity, float original);

    // 链式修改玩家对特定方块的挖掘强度
    float onPlayerRawStrVsBlockModify(EntityPlayer player, Item tool, Block block,
            int metadata, float original);

    // 链式修改玩家通用挖掘强度
    float onPlayerStrVsBlockModify(EntityPlayer player, float original);

    // 链式修改玩家受到的击退值
    float onPlayerReceiveKnockBackModify(EntityPlayer player, Entity attacker, float original);

    // 链式修改生物摔落伤害,此处理在蜘蛛摔落检测之后执行。参见{@link EntityLivingBase#fall(float)}
    float onEntityLivingFallDamageModify(EntityLivingBase instance, float fall_distance,
            BlockInfo block_landed_on_info, float original);
}
```

> **链式修改**：返回值作为下一个 listener 的输入参数。最后一个 listener 的返回值即为最终值。

---

### 1.3 EntityEventHandler — 实体生命周期

**Listener 接口**: `IEntityEventListener`
**全限定名**: `moddedmite.rustedironcore.api.event.listener.IEntityEventListener`

```java
/**
 * 实体事件监听器接口。
 * 用于监听各类实体生命周期中的事件，如生成、死亡、攻击、掉落等。
 * 注意：部分方法仅在服务端生效，客户端调用无效。
 */
public interface IEntityEventListener {

    /**
     * 当实体拾取战利品（如杀死生物后掉落物品）时触发。
     * <p><b>注意：</b>此方法仅在服务端调用，客户端不会触发。
     *
     * @param entity 拾取战利品的实体（通常是玩家或生物）
     * @param cause  造成此次拾取的原因（例如伤害来源）
     */
    void onLoot(EntityLivingBase entity, DamageSource cause);       // SERVER only

    /**
     * 当实体生成（进入世界）时触发。
     * 包括玩家登录、生物自然生成、刷怪蛋生成等情况。
     *
     * @param entity 生成的实体
     */
    void onSpawn(Entity entity);

    /**
     * 当实体死亡时触发。
     * 注意：此方法在实体生命值归零且死亡逻辑执行后调用。
     *
     * @param entity 死亡的实体（必须是 EntityLivingBase）
     * @param cause  死亡原因（伤害来源）
     */
    void onDeath(EntityLivingBase entity, DamageSource cause);

    /**
     * 当服务端玩家死亡时触发。
     * <p><b>注意：</b>此方法仅在服务端调用，客户端不会触发。
     * 与 {@link #onDeath(EntityLivingBase, DamageSource)} 区别在于参数为 ServerPlayer，可获取更多服务端特有信息。
     *
     * @param player 死亡的服务器玩家对象
     * @param cause  死亡原因（伤害来源）
     */
    void onServerPlayerDeath(ServerPlayer player, DamageSource cause); // SERVER

    /**
     * 当实体每帧更新（tick）时触发。
     * 适用于持续监控实体状态或执行周期性逻辑。
     *
     * @param entity 正在更新的实体
     */
    void onUpdate(EntityLivingBase entity);

    /**
     * 当实体受到攻击（即伤害计算前）时触发。
     * 可用于修改伤害值或执行前置逻辑。
     *
     * @param target 被攻击的目标实体
     * @param damage 伤害对象（包含伤害数值、类型等信息）
     */
    void onAttackEntityFrom(EntityLivingBase target, Damage damage);

    /**
     * 当实体从高处掉落时触发。
     * 掉落距离达到一定值（通常大于 3 格）时调用。
     *
     * @param entity   掉落的实体
     * @param distance 掉落距离（格数）
     */
    void onFall(EntityLivingBase entity, float distance);

    /**
     * 当实体跳跃时触发。
     * 适用于需要响应跳跃动作的场合，如统计或特殊效果。
     *
     * @param entity 跳跃的实体
     */
    void onJump(EntityLivingBase entity);
}
```

**Listener 接口**: `IEntityMobListener`
**全限定名**: `moddedmite.rustedironcore.api.event.listener.IEntityMobListener`

```java
/**
 * 实体怪物（敌对生物）监听器接口。
 * 用于监听 {@link EntityMob} 在 NBT 数据读写时的回调事件，
 * 方便在数据持久化或加载过程中执行额外逻辑（如自定义字段的序列化/反序列化）。
 */
public interface IEntityMobListener {

    /**
     * 当实体怪物从 NBT 标签中读取数据（反序列化）时触发。
     * 通常发生在实体从磁盘加载到世界中时（如区块加载、实体重生）。
     * 可在此方法中读取自定义的 NBT 数据，并应用到实体对象上。
     * <p><b>注意：</b>此方法在实体对象创建后、添加到世界前调用，此时实体尚未完全初始化。
     *
     * @param mob 正在读取 NBT 的实体怪物对象
     * @param nbt 包含实体数据的 NBT 复合标签（只读，但可从中获取数据）
     */
    void onReadEntityFromNBT(EntityMob mob, NBTTagCompound nbt);

    /**
     * 当实体怪物将数据写入 NBT 标签（序列化）时触发。
     * 通常发生在实体需要保存到磁盘时（如区块保存、实体卸载）。
     * 可在此方法中向 NBT 写入自定义字段，以确保数据持久化。
     * <p><b>注意：</b>此方法在实体数据保存前调用，此时实体状态完整。
     *
     * @param mob 正在写入 NBT 的实体怪物对象
     * @param nbt 用于存储实体数据的 NBT 复合标签（可修改，写入自定义数据）
     */
    void onWriteEntityToNBT(EntityMob mob, NBTTagCompound nbt);
}
```

---

### 1.4 PlayerEventHandler — 玩家事件

**Listener 接口**: `IPlayerEventListener`
**全限定名**: `moddedmite.rustedironcore.api.event.listener.IPlayerEventListener`

```java
/**
 * 玩家事件监听器接口。
 * 用于监听玩家在游戏过程中的关键生命周期事件，包括登录、登出、重生以及跨维度传送等。
 * 实现此接口并注册到事件总线，即可响应相应的玩家行为。
 */
public interface IPlayerEventListener {

    /**
     * 当玩家成功登录服务器时触发。
     * <p>此时玩家对象已完全初始化，且已加载到世界中，可以安全地获取玩家数据或执行欢迎逻辑。
     *
     * @param event 玩家登录事件对象，包含登录的玩家、登录时间等信息
     */
    void onPlayerLoggedIn(PlayerLoggedInEvent event);

    /**
     * 当玩家登出服务器时触发。
     * <p>此事件在玩家断开连接、会话过期或服务端主动踢出玩家时调用。
     * 可用于保存玩家数据、清理资源或记录登出日志。
     *
     * @param event 玩家登出事件对象，包含登出的玩家及其登出原因
     */
    void onPlayerLoggedOut(PlayerLoggedOutEvent event);

    /**
     * 当玩家重生时触发。
     * <p>包括死亡后点击“重生”按钮，以及通过指令或插件强制重生的情况。
     * 此时玩家已重新生成到重生点（如床或世界出生点），生命值、饥饿度等已恢复。
     *
     * @param event 玩家重生事件对象，包含重生玩家、重生位置、是否因死亡而重生等信息
     */
    void onPlayerRespawn(PlayerRespawnEvent event);

    /**
     * 当玩家跨越维度（如从主世界到地狱）时触发。
     * <p>此事件在维度切换前或切换后调用（取决于具体实现），可用于处理跨维度时的数据转换、传送门效果等。
     *
     * @param event 玩家跨维度事件对象，包含玩家、源维度、目标维度以及传送方式等信息
     */
    void onPlayerCrossDimension(PlayerCrossDimensionEvent event);
}
```

---

### 1.5 TickHandler — Tick 循环

**Listener 接口**: `ITickListener`
**全限定名**: `moddedmite.rustedironcore.api.event.listener.ITickListener`

```java
/**
 * 游戏刻（Tick）事件监听器接口。
 * 用于监听游戏循环中各个阶段的更新事件，包括客户端/服务端刻、实体刻、渲染刻等。
 * 注意：部分方法仅在客户端有效，服务端调用时不会触发。
 */
public interface ITickListener {

    /**
     * 当玩家实体（{@link EntityPlayer}）进行刻更新（tick）时触发。
     * 此方法在玩家自身的 update 逻辑中调用，无论客户端还是服务端均会执行。
     * 适用于监控玩家状态、执行周期性玩家相关逻辑。
     *
     * @param player 正在执行刻更新的玩家实体
     */
    void onEntityPlayerTick(EntityPlayer player);

    /**
     * 当客户端主循环进行刻更新（tick）时触发。
     * <p><b>注意：</b>此方法仅在客户端调用，服务端不会触发。
     * 可用于处理客户端特有的更新逻辑，如渲染准备、输入处理等。
     *
     * @param client Minecraft 客户端实例（{@link Minecraft}），提供客户端状态访问
     */
    void onClientTick(Minecraft client);         // CLIENT only

    /**
     * 当服务端主循环进行刻更新（tick）时触发。
     * <p><b>注意：</b>此方法仅在客户端调用（因为服务端逻辑由内置服务端处理），
     * 实际上该方法通常用于客户端集成服务端（如单机内置服务端）的刻事件。
     * 若在纯客户端环境下，此方法可能不会触发或参数为 null。
     *
     * @param server Minecraft 服务端实例（{@link MinecraftServer}），提供服务端状态访问
     */
    void onServerTick(MinecraftServer server);    // CLIENT only

    /**
     * 当客户端进行渲染刻（render tick）时触发。
     * <p><b>注意：</b>此方法仅在客户端调用，服务端不会触发。
     * 每帧渲染前调用，可用于更新与渲染相关的状态（如动画进度、粒子效果等）。
     *
     * @param partialTick 部分刻的插值因子（0.0 ~ 1.0），用于平滑渲染插值
     */
    void onRenderTick(float partialTick);        // CLIENT only

    /**
     * 当任意实体（包括非玩家实体）进行刻更新（tick）时触发。
     * 此方法在实体的通用更新逻辑中调用，可用于监控所有实体的状态变化。
     *
     * @param entity 正在执行刻更新的实体（可以是任意类型）
     */
    void onEntityTick(Entity entity);
}
```

---

### 1.6 PlayerAttributeHandler — 玩家属性

**Listener 接口**: `IPlayerAttributeListener`
**全限定名**: `moddedmite.rustedironcore.api.event.listener.IPlayerAttributeListener`

```java
public interface IPlayerAttributeListener {
    float onHealthLimitModify(EntityPlayer player, float original);    // 链式修改血量上限
    int onLevelLimitModify(int original);                              // 链式修改等级上限
    int onLevelMinLimitModify(int original);                           // 链式修改最低等级
    int onHungerLimitModify(EntityPlayer player, int original);        // 链式修改饥饿上限
    int onSaturationLimitModify(EntityPlayer player, int original);    // 链式修改饱和上限
    int onNutritionLimitModify(EntityPlayer player, int original);     // 链式修改营养上限
    int onInsulinResistanceLimitModify(EntityPlayer player, int original); // 链式修改胰岛素抵抗上限
    int onNutritionInitModify(EntityPlayer player, int original);      // 链式修改营养初始值
}
```

---

### 1.7 其他 Handler

以下 Handler 均通过 `Handlers.X.register(new IXXXListener() { ... });` 注册。

#### IInitializationListener

用途: 在客户端/服务端启动完成后获得回调，适合注册需要延迟执行的初始化代码。

注册示例:
```java
Handlers.Initialization.register(new IInitializationListener() {
    @Override
    public void onClientStarted(Minecraft client) {
        // 客户端启动完成后执行
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        // 服务器启动完成后执行
    }
});
```

```java
public interface IInitializationListener {
    void onClientStarted(Minecraft client);        // CLIENT only
    void onServerStarted(MinecraftServer server);  // SERVER only
}
```

#### IScreenListener

用途: 监听 GUI 屏幕生命周期，用于自定义按钮、绘制、输入拦截等客户端 GUI 扩展。

注册示例:
```java
Handlers.Screen.register(new IScreenListener() {
    @Override
    public void onInit(GuiScreen screen, List<GuiButton> buttonList) { }
    // ...
});
```

```java
/**
 * GUI 屏幕（{@link GuiScreen}）事件监听器接口。
 * 用于监听屏幕的初始化、绘制、关闭、鼠标/键盘输入等各种交互事件。
 * 实现此接口并注册，可在屏幕生命周期中插入自定义逻辑（如添加按钮、修改渲染等）。
 * <p><b>注意：</b>此接口所有方法均仅在客户端有效，服务端不会触发。
 */
public interface IScreenListener {

    /**
     * 当屏幕初始化（{@link GuiScreen#initGui()}）时触发。
     * 此时屏幕组件（如按钮、标签）正在构建，可在此方法中添加或修改按钮列表。
     *
     * @param screen     正在初始化的屏幕对象
     * @param buttonList 当前屏幕的按钮列表（可修改，直接添加或移除按钮）
     */
    void onInit(GuiScreen screen, List<GuiButton> buttonList);

    /**
     * 当屏幕关闭（即从屏幕栈中弹出或玩家关闭GUI）时触发。
     * 可用于释放资源、保存临时状态或执行清理操作。
     *
     * @param screen 即将关闭的屏幕对象
     */
    void onClose(GuiScreen screen);

    /**
     * 当屏幕每帧更新（{@link GuiScreen#updateScreen()}）时触发。
     * 适用于需要持续更新屏幕状态（如动画、计时器）的逻辑。
     *
     * @param screen 正在更新的屏幕对象
     */
    void onUpdate(GuiScreen screen);

    /**
     * 当屏幕绘制（{@link GuiScreen#drawScreen(int, int, float)}）时触发。
     * 注意：此方法在绘制前景层（包括按钮、文本等）时调用。
     * 若需绘制背景，请使用 {@link #onDrawBackground(GuiScreen)}。
     *
     * @param screen       正在绘制的屏幕对象
     * @param mouseX       鼠标当前 X 坐标（屏幕坐标）
     * @param mouseY       鼠标当前 Y 坐标（屏幕坐标）
     * @param partialTicks 部分刻的插值因子（用于平滑动画）
     */
    void onDraw(GuiScreen screen, int mouseX, int mouseY, float partialTicks);

    /**
     * 当屏幕绘制背景层时触发。
     * 在 {@link #onDraw(GuiScreen, int, int, float)} 之前调用，专门用于绘制屏幕背景（如半透明遮罩、背景纹理）。
     *
     * @param screen 正在绘制的屏幕对象
     */
    void onDrawBackground(GuiScreen screen);

    /**
     * 当屏幕中的按钮被点击时触发。
     *
     * @param screen     当前屏幕对象
     * @param buttonList 当前屏幕的按钮列表
     * @param button     被点击的按钮对象
     */
    void onButtonAction(GuiScreen screen, List<GuiButton> buttonList, GuiButton button);

    /**
     * 当鼠标在屏幕区域内被点击时触发。
     * 对应 {@link GuiScreen#mouseClicked(int, int, int)}。
     *
     * @param screen  当前屏幕对象
     * @param mouseX  鼠标点击的 X 坐标
     * @param mouseY  鼠标点击的 Y 坐标
     * @param button  鼠标按键编号（0=左键，1=右键，2=中键）
     */
    void onMouseClicked(GuiScreen screen, int mouseX, int mouseY, int button);

    /**
     * 当鼠标按键被释放时触发。
     * 对应 {@link GuiButton#mouseReleased(int, int, int)}。
     *
     * @param screen  当前屏幕对象
     * @param mouseX  鼠标释放时的 X 坐标
     * @param mouseY  鼠标释放时的 Y 坐标
     * @param button  释放的鼠标按键编号
     */
    void onMouseReleased(GuiScreen screen, int mouseX, int mouseY, int button);

    /**
     * 当键盘按键被按下并释放（即键入字符）时触发。
     * 对应 {@link GuiScreen#keyTyped(char, int)}。
     * 可用于处理文本输入或快捷键。
     *
     * @param screen     当前屏幕对象
     * @param typedChar  实际输入的字符（如 'a'，'1'）
     * @param code       键盘键码（如 KeyCode.KEY_ESCAPE）
     */
    void onKeyTyped(GuiScreen screen, char typedChar, int code);

    /**
     * 当鼠标输入事件发生时触发（包括移动、点击、滚轮等）。
     * 对应 {@link GuiScreen#handleMouseInput()}。
     * 注意：此方法在鼠标事件分发之前调用，可用于全局拦截或统计。
     *
     * @param screen 当前屏幕对象
     */
    void onMouseInput(GuiScreen screen);

    /**
     * 当键盘输入事件发生时触发（包括按键按下和释放）。
     * 对应 {@link GuiScreen#handleKeyboardInput()}。
     * 注意：此方法在键盘事件分发之前调用，可用于全局监听或屏蔽某些按键。
     *
     * @param screen 当前屏幕对象
     */
    void onKeyboardInput(GuiScreen screen);
}
```

#### IAchievementListener

用途: 监听成就相关事件，例如物品合成、拾取、熔炼和维度旅行。

注册示例:
```java
Handlers.Achievement.register(new IAchievementListener() {
    @Override
    public void onItemCrafted(EntityPlayer player, ItemStack result) { }
    // ...
});
```

```java
/**
 * 成就系统事件监听器接口。
 * 用于监听玩家在游戏过程中可能触发成就的相关行为，包括合成物品、拾取物品、熔炼物品以及跨维度移动等。
 * 实现此接口并注册，可在特定行为发生时执行自定义逻辑（如解锁成就、记录统计数据等）。
 */
public interface IAchievementListener {

    /**
     * 当玩家合成（Craft）物品时触发。
     * <p>包括工作台合成、合成表合成等操作。此事件在物品合成成功后、产物进入玩家背包前调用。
     * 可用于记录合成次数、解锁合成类成就。
     *
     * @param player 执行合成的玩家
     * @param result 合成产出的物品堆（包含数量和NBT数据）
     */
    void onItemCrafted(EntityPlayer player, ItemStack result);

    /**
     * 当玩家拾取地面上的物品时触发。
     * <p>包括捡起掉落的实体物品（EntityItem）和从容器中取出物品等情况。
     * 此事件在物品进入玩家背包后调用。
     *
     * @param player 拾取物品的玩家
     * @param picked 被拾取的物品堆（可能包含多个数量）
     */
    void onItemPickUp(EntityPlayer player, ItemStack picked);

    /**
     * 当玩家通过熔炉（Furnace）熔炼物品时触发。
     * <p>此事件在熔炼完成、产物进入输出槽时调用。
     * 可用于统计熔炼次数或解锁熔炼相关成就。
     *
     * @param player 进行熔炼操作的玩家（如果熔炉由玩家触发）
     * @param result 熔炼产出的物品堆
     */
    void onItemSmelt(EntityPlayer player, ItemStack result);

    /**
     * 当玩家跨维度传送时触发。
     * <p>包括通过传送门（地狱门、末地门）、命令或插件传送等。
     * 可用于记录玩家探索维度次数或解锁跨维度成就。
     *
     * @param player 传送的玩家
     * @param fromDim 源维度的 ID（如 0=主世界，-1=地狱，1=末地）
     * @param toDim   目标维度的 ID
     */
    void onDimensionTravel(EntityPlayer player, int fromDim, int toDim);
}
```

#### IFurnaceUpdateListener

用途: 修改熔炉工作过程、燃烧时间和 CookTime 行为。

注册示例:
```java
Handlers.FurnaceUpdate.register(new IFurnaceUpdateListener() {
    @Override
    public void onFurnaceUpdatePre(TileEntityFurnace tileEntityFurnace) { }
    // ...
});
```

```java
/**
 * 熔炉（{@link TileEntityFurnace}）更新事件监听器接口。
 * 用于监听熔炉在烧炼过程中的各类状态变化，包括燃料消耗、烹饪进度、燃烧开始/结束等。
 * 实现此接口并注册，可对熔炉的行为进行扩展或修改（如自定义燃料效率、烹饪速度等）。
 * <p><b>注意：</b>熔炉逻辑主要在服务端执行，因此大部分方法仅在服务端有效。
 */
public interface IFurnaceUpdateListener {

    /**
     * 在熔炉执行 {@link TileEntityFurnace#update()} 逻辑之前触发。
     * 可用于在熔炉状态更新前执行预处理，如记录当前状态、修改即将使用的变量等。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     */
    void onFurnaceUpdatePre(TileEntityFurnace tileEntityFurnace);

    /**
     * 修改熔炉燃料燃烧时间每次减少的值（即每游戏刻减少的燃烧时间量）。
     * 默认情况下，熔炉每刻减少 1 个单位的燃烧时间。
     * <p>通过此方法可修改减少量，例如降低为 0 可无限燃烧，或增加以加速燃料消耗。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     * @param original          原始减少量（通常为 1）
     * @return 修改后的减少量（必须大于等于 0，否则可能导致异常）
     */
    int onFurnaceBurnTimeDecreaseModify(TileEntityFurnace tileEntityFurnace, int original);

    /**
     * 在熔炉决定是否开始燃烧（即消耗燃料）时触发。
     * 通过返回值可控制是否允许开始燃烧。
     * <p>注意：此方法在燃料被消耗前调用，若返回 false 则本次不会消耗燃料也不会开始燃烧。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     * @param original          原始决定（通常由逻辑判断得出，如有燃料且可烧炼则 true）
     * @return 修改后的决定（true 表示允许燃烧，false 表示阻止燃烧）
     */
    boolean onFurnaceBeginToBurn(TileEntityFurnace tileEntityFurnace, boolean original);

    /**
     * 当熔炉消耗一份燃料时触发。
     * 燃料被消耗（即燃烧时间减少到 0 或开始新的燃烧）时调用。
     * 可用于记录燃料消耗日志、触发额外效果（如产生经验）等。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     */
    void onFurnaceFuelConsumed(TileEntityFurnace tileEntityFurnace);

    /**
     * 当熔炉的烹饪时间（cookTime）增加时触发（即每刻增加一次）。
     * 此时熔炉正在烧炼物品，且烹饪时间已增加。
     * 可用于触发进度更新或自定义效果。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     */
    void onFurnaceCookTimeAdd(TileEntityFurnace tileEntityFurnace);

    /**
     * 修改熔炉每刻烹饪时间的增加量（即烹饪速度）。
     * 默认每次增加 1，修改此值可加速或减速烧炼过程。
     * <p>例如，设置为 2 则烹饪速度翻倍，设置为 0 则暂停烹饪。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     * @param original          原始增加量（通常为 1）
     * @return 修改后的增加量（必须大于等于 0）
     */
    int onFurnaceCookTimeIncreaseModify(TileEntityFurnace tileEntityFurnace, int original);

    /**
     * 修改熔炉完成一次烧炼所需的目标烹饪时间（cookTimeTotal）。
     * 默认值取决于烧炼配方（通常为 200 刻，即 10 秒）。
     * 修改此值可改变单个物品烧炼所需的总时长。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     * @param original          原始目标时间（由配方决定）
     * @return 修改后的目标时间（必须大于 0）
     */
    int onFurnaceCookTimeTargetModify(TileEntityFurnace tileEntityFurnace, int original);

    /**
     * 当熔炉成功烧炼完成一个物品（烹饪时间达到目标）时触发。
     * 此时产物已生成，燃料可能已消耗，此方法在产物放入输出槽后调用。
     * 可用于统计烧炼次数、解锁成就或触发额外掉落。
     *
     * @param tileEntityFurnace 当前熔炉方块实体对象
     */
    void onFurnaceCookSuccess(TileEntityFurnace tileEntityFurnace);
}
```

#### IEnchantingListener

用途: 调整附魔最大数量和附魔界面选择的附魔等级。

注册示例:
```java
Handlers.Enchanting.register(new IEnchantingListener() {
    @Override
    public int onMaxEnchantNumModify(Random random, ItemStack item_stack, int enchantment_levels, int original) {
        return original;
    }
    // ...
});
```

```java
public interface IEnchantingListener {
    int onMaxEnchantNumModify(Random random, ItemStack item_stack, int enchantment_levels, int original);  // 链式修改最大附魔数量
    int onEnchantLevelModify(ContainerEnchantment containerEnchantment, int slot_index, int original);      // 链式修改附魔等级
}
```

#### ITooltipListener (CLIENT only)

用途: 客户端物品提示文字自定义扩展。

注册示例:
```java
Handlers.Tooltip.register(new ITooltipListener() {
    @Override
    public void onTooltipHead(ItemStack stack, List<String> tooltip, EntityPlayer player, boolean detailed, Slot slot) { }
    // ...
});
```

```java
public interface ITooltipListener {
    void onTooltipHead(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);
    void onTooltipNeck(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);
    void onTooltipBody(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);
    void onTooltipWaist(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);
    void onTooltipTail(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);
}
```

#### ILootTableRegisterListener

用途: 修改或追加结构、地牢、钓鱼等战利品表。

注册示例:
```java
Handlers.LootTable.register(new ILootTableRegisterListener() {
    @Override
    public void onDungeonOverworldRegister(List<WeightedRandomChestContent> original) { }
    // ...
});
```

```java
/**
 * 物品工具提示（Tooltip）监听器接口。
 * 用于在物品的提示框（Hover Tooltip）构建时，按照特定的顺序（头部→颈部→身体→腰部→尾部）
 * 插入或修改工具提示文本。每个方法对应不同的插入阶段，方便实现分层次的提示内容管理。
 * <p><b>注意：</b>此接口仅在客户端有效，用于渲染物品提示信息。
 *
 * @see net.minecraft.item.ItemStack
 * @see net.minecraft.entity.player.EntityPlayer
 * @see net.minecraft.inventory.Slot
 */
public interface ITooltipListener {

    /**
     * 在工具提示的 <b>头部</b> 阶段触发（最早执行的插入点）。
     * 通常用于添加在物品名称之前的内容（如品质前缀、MOD 来源等）。
     *
     * @param stack    当前要显示提示的物品堆
     * @param tooltip  工具提示文本列表（可修改，直接添加或插入元素）
     * @param player   查看该提示的玩家（可能为 null）
     * @param detailed 是否为详细模式（如按住 Shift 键时触发的详细提示）
     * @param slot     物品所在的格子（若在容器中则为对应 Slot，否则可能为 null）
     */
    void onTooltipHead(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);

    /**
     * 在工具提示的 <b>颈部</b> 阶段触发（紧接在头部之后）。
     * 适合添加与物品核心属性相关的次要信息（如耐久度、附魔信息的前缀等）。
     *
     * @param stack    当前要显示提示的物品堆
     * @param tooltip  工具提示文本列表（可修改）
     * @param player   查看该提示的玩家（可能为 null）
     * @param detailed 是否为详细模式
     * @param slot     物品所在的格子（可能为 null）
     */
    void onTooltipNeck(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);

    /**
     * 在工具提示的 <b>身体</b> 阶段触发（主要信息区）。
     * 这是最核心的插入点，通常用于显示物品的基础描述、属性数值、附加效果等。
     *
     * @param stack    当前要显示提示的物品堆
     * @param tooltip  工具提示文本列表（可修改）
     * @param player   查看该提示的玩家（可能为 null）
     * @param detailed 是否为详细模式
     * @param slot     物品所在的格子（可能为 null）
     */
    void onTooltipBody(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);

    /**
     * 在工具提示的 <b>腰部</b> 阶段触发（身体之后的补充区域）。
     * 适合添加次要属性、额外说明或装饰性分隔符。
     *
     * @param stack    当前要显示提示的物品堆
     * @param tooltip  工具提示文本列表（可修改）
     * @param player   查看该提示的玩家（可能为 null）
     * @param detailed 是否为详细模式
     * @param slot     物品所在的格子（可能为 null）
     */
    void onTooltipWaist(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);

    /**
     * 在工具提示的 <b>尾部</b> 阶段触发（最后执行的插入点）。
     * 通常用于添加最末尾的信息（如模组名称、版权标记、操作提示等）。
     *
     * @param stack    当前要显示提示的物品堆
     * @param tooltip  工具提示文本列表（可修改）
     * @param player   查看该提示的玩家（可能为 null）
     * @param detailed 是否为详细模式
     * @param slot     物品所在的格子（可能为 null）
     */
    void onTooltipTail(ItemStack stack, List<String> tooltip,
            EntityPlayer player, boolean detailed, Slot slot);
}
```

#### IGravelDropListener

用途: 自定义砾石块掉落砾石/燧石的概率和掉落结果。

注册示例:
```java
Handlers.GravelDrop.register(new IGravelDropListener() {
    @Override
    public float onDropAsGravelChanceModify(BlockBreakInfo info, float original) {
        return original;
    }
    // ...
});
```

```java
/**
 * 沙砾（Gravel）掉落事件监听器接口。
 * 用于监听玩家破坏沙砾类方块时的掉落概率、掉落物品ID以及最终掉落结果。
 * 通过实现此接口，可以自定义沙砾掉落物（如沙砾本身、燧石、下界沙砾产物等）的概率和类型。
 * <p>所有方法均为默认方法，默认返回原始值或空实现，无需强制覆写，按需扩展即可。
 * <p><b>注意：</b>沙砾掉落逻辑主要在服务端执行，因此这些方法通常仅在服务端生效。
 *
 * @see net.minecraft.BlockGravel
 * @see net.minecraft.ItemStack
 */
public interface IGravelDropListener {

    /**
     * 修改破坏沙砾时掉落沙砾（Gravel）本身的概率。
     *
     * @param info     破坏方块的信息（包含玩家、位置、方块状态等）
     * @param original 原始的掉落沙砾概率（浮点数，范围 0.0 ~ 1.0）
     * @return 修改后的概率值（0.0 ~ 1.0），若返回 original 表示不修改
     */
    default float onDropAsGravelChanceModify(BlockBreakInfo info, float original) { return original; }

    /**
     * 修改破坏沙砾时掉落燧石（Flint）的概率。
     *
     * @param info     破坏方块的信息
     * @param original 原始的掉落燧石概率（浮点数，范围 0.0 ~ 1.0）
     * @return 修改后的概率值（0.0 ~ 1.0），若返回 original 表示不修改
     */
    default float onDropAsFlintChanceModify(BlockBreakInfo info, float original) { return original; }

    /**
     * 修改破坏沙砾时掉落燧石碎片（Chip）的概率
     *
     * @param info     破坏方块的信息
     * @param original 原始的燧石变碎片概率（浮点数，范围 0.0 ~ 1.0）
     * @return 修改后的概率值（0.0 ~ 1.0），若返回 original 表示不修改
     */
    default float onDropFlintAsChipChanceModify(BlockBreakInfo info, float original) { return original; }

    /**
     * 修改下界沙砾（Nether Gravel）掉落的物品 ID。
     *
     * @param info     破坏方块的信息
     * @param original 原始的掉落物 ID
     * @return 修改后的掉落物 ID，若返回 original 表示不修改
     */
    default int onNetherGravelDropIDModify(BlockBreakInfo info, int original) { return original; }

    /**
     * 在沙砾掉落过程最终完成后触发，提供最终掉落的物品 ID。
     * <p>此方法在所有概率计算和物品选择之后调用，可用于记录掉落结果、统计或触发后续效果。
     * 注意：此方法没有返回值，仅作为回调通知。
     *
     * @param info       破坏方块的信息
     * @param id_dropped 最终掉落的物品 ID（可能是沙砾、燧石或自定义物品的ID）
     */
    default void onDropResult(BlockBreakInfo info, int id_dropped) {}
}
```

#### IConnectionListener

用途: 监听客户端/服务器连接过程、登录和断开事件。

注册示例:
```java
Handlers.Connection.register(new IConnectionListener() {
    @Override
    public void onClientConnection(NetClientHandler clientHandler, String server, int port) { }
    // ...
});
```

```java
/**
 * 网络连接事件监听器接口。
 * 用于监听客户端与服务器的连接、登录及断开事件。
 * 提供客户端连接远程服务器、集成服务器（单机）连接、登录成功和断开连接时的回调。
 * <p><b>注意：</b>部分方法仅在客户端环境下有效（通过 {@link Environment} 标记），
 * 在服务端调用这些方法不会被触发或可能引发异常。
 *
 * @see net.minecraft.network.NetClientHandler
 * @see net.minecraft.server.MinecraftServer
 * @see net.minecraft.network.packet.Packet1Login
 * @see net.minecraft.network.packet.Packet255KickDisconnect
 */
public interface IConnectionListener {

    /**
     * 当客户端与远程服务器建立连接时触发。
     * <p>此事件在客户端发起连接、NetClientHandler 初始化后调用。
     * 可用于记录连接信息、执行连接前的准备工作或修改连接参数。
     * <p><b>注意：</b>此方法仅在客户端环境中调用。
     *
     * @param clientHandler 客户端的网络处理器，用于处理与服务器的通信
     * @param server        目标服务器的地址（域名或IP）
     * @param port          目标服务器的端口号
     */
    @Environment(EnvType.CLIENT)
    default void onClientConnection(NetClientHandler clientHandler, String server, int port) {}

    /**
     * 当客户端与集成服务器（即单人游戏内置服务器）建立连接时触发。
     * <p>此事件在玩家启动单人游戏世界、集成服务器启动并与客户端建立连接时调用。
     * 可用于识别是否为单机模式，或执行针对集成环境的特殊逻辑。
     * <p><b>注意：</b>此方法可能在客户端和服务端同时存在，但参数中的 NetClientHandler 为客户端对象，
     * 具体调用环境取决于实现（通常仅在客户端触发）。
     *
     * @param clientHandler 客户端的网络处理器
     * @param server        集成服务器实例（MinecraftServer），可用于获取服务器状态
     */
    default void onIntegratedConnection(NetClientHandler clientHandler, MinecraftServer server) {}

    /**
     * 当客户端成功登录服务器（即完成登录握手）时触发。
     * <p>此事件在客户端接收到服务器返回的 {@link Packet1Login} 登录包后调用，
     * 表示玩家已被服务器认证并允许进入世界。
     * 可用于初始化客户端玩家数据、触发欢迎消息或统计登录信息。
     * <p><b>注意：</b>此方法仅在客户端环境中调用。
     *
     * @param clientHandler 客户端的网络处理器
     * @param login         登录数据包，包含玩家实体ID、游戏模式、维度等信息
     */
    @Environment(EnvType.CLIENT)
    default void onClientLoggedIn(NetClientHandler clientHandler, Packet1Login login) {}

    /**
     * 当客户端从服务器断开连接（被踢出或主动退出）时触发。
     * <p>此事件在客户端收到断开连接数据包（{@link Packet255KickDisconnect}）后调用，
     * 可能由于服务器关闭、玩家主动退出或被管理员踢出等原因。
     * 可用于释放网络资源、记录断开原因或执行清理操作。
     * <p><b>注意：</b>此方法仅在客户端环境中调用。
     *
     * @param clientHandler 客户端的网络处理器（此时可能已断开）
     * @param disconnect    断开连接的数据包，包含原因描述（如“已退出”或“被踢出”）
     */
    @Environment(EnvType.CLIENT)
    default void onClientQuit(NetClientHandler clientHandler, Packet255KickDisconnect disconnect) {}
}
```

#### IChunkLoadListener

用途: 监听区块加载与卸载，支持客户端与服务端。

注册示例:
```java
Handlers.ChunkLoad.register(new IChunkLoadListener() {
    @Override
    public void onServerChunkLoad(Chunk chunk) { }
    // ...
});
```

```java
/**
 * 区块（Chunk）加载与卸载事件监听器接口。
 * 用于监听客户端或服务端区块的加载和卸载事件，分别在对应的逻辑端触发。
 * <p>所有方法均为默认方法，默认空实现，可根据需要选择性覆写。
 * <p><b>注意：</b>客户端方法仅在客户端环境调用，服务端方法仅在服务端环境调用。
 *
 * @see net.minecraft.world.chunk.Chunk
 */
public interface IChunkLoadListener {

    /**
     * 当客户端加载区块时触发。
     * <p>此方法仅在客户端环境中调用，服务端不会触发。
     * 可用于客户端特有的区块初始化（如预加载渲染资源、更新小地图等）。
     *
     * @param chunk 被加载的区块对象
     */
    default void onClientChunkLoad(Chunk chunk) {}

    /**
     * 当客户端卸载区块时触发。
     * <p>此方法仅在客户端环境中调用，服务端不会触发。
     * 可用于清理客户端持有的区块相关资源（如释放缓存、更新显示等）。
     *
     * @param chunk 被卸载的区块对象
     */
    default void onClientChunkUnload(Chunk chunk) {}

    /**
     * 当服务端加载区块时触发。
     * <p>此方法仅在服务端环境中调用，客户端不会触发。
     * 可用于服务端的区块数据初始化（如加载实体、触发势力生成等）。
     *
     * @param chunk 被加载的区块对象
     */
    default void onServerChunkLoad(Chunk chunk) {}

    /**
     * 当服务端卸载区块时触发。
     * <p>此方法仅在服务端环境中调用，客户端不会触发。
     * 可用于服务端的区块清理工作（如保存数据、移除实体等）。
     *
     * @param chunk 被卸载的区块对象
     */
    default void onServerChunkUnload(Chunk chunk) {}
}
```

#### IBiomeGenerateListener

用途: 拦截群系生成过程和结构群系注册，适合修改地图层生成逻辑。

注册示例:
```java
Handlers.BiomeGenerate.register(new IBiomeGenerateListener() {
    @Override
    public void onInitialBiomesModify(List<BiomeGenBase> list) { }
    // ...
});
```

```java
/**
 * 生物群系（Biome）生成事件监听器接口。
 * 用于监听世界生成过程中生物群系初始化、特定地形层（Layer）的行为修改，
 * 以及各类结构（要塞、村庄等）允许生成的生物群系列表注册。
 * <p>所有方法均为默认方法，默认返回原始值或空实现，无需强制覆写，按需扩展即可。
 * <p><b>注意：</b>此类方法主要在世界生成阶段（World Generation）调用，仅服务端有效。
 *
 * @see net.minecraft.world.biome.BiomeGenBase
 * @see net.minecraft.world.gen.layer.GenLayer
 */
public interface IBiomeGenerateListener {

    /**
     * 修改初始生物群系列表（在生物群系生成器初始化时调用的原始列表）。
     * <p>此方法在生物群系注册之后、生成层（GenLayer）构建之前调用，可对默认的生物群系列表进行增删改，
     * 从而影响整个世界生成的群系构成。
     * <p><b>注意：</b>此列表为原始引用，直接修改其内容会影响后续所有生物群系的生成逻辑。
     *
     * @param list 生物群系列表（可修改），包含所有可用的生物群系实例
     */
    default void onInitialBiomesModify(List<BiomeGenBase> list) {}

    /**
     * 修改 {@link GenLayer#addSnow} 层的行为，即控制某个坐标点是否应被设定为雪地生物群系。
     * <p>此方法在生物群系生成链中的特定层调用，可通过修改返回值来干预雪地群系的生成逻辑。
     *
     * @param genLayer 当前的生成层实例，包含上下文信息
     * @param original 原始的返回值（代表该点是否为雪地群系的判定标志）
     * @return 修改后的判定值（通常为特定生物群系ID或标志位），若返回 original 表示不修改
     */
    default int onLayerAddSnow(GenLayer genLayer, int original) { return original; }

    /**
     * 修改 {@link GenLayer#addIsland} 层的行为，即控制岛屿状生物群系（如蘑菇岛）的生成。
     * <p>此方法在生物群系生成链中调用，用于决定某点是否应被替换为岛屿类生物群系。
     *
     * @param genLayer 当前的生成层实例
     * @param original 原始的返回值（代表该点是否为岛屿群系的判定标志）
     * @return 修改后的判定值，若返回 original 表示不修改
     */
    default int onLayerAddIsland(GenLayer genLayer, int original) { return original; }

    /**
     * 修改 {@link GenLayer#addHills} 层的行为，即控制丘陵地形生物群系（如森林丘陵、沙漠丘陵）的生成。
     * <p>此方法在生物群系生成链中调用，用于决定某点是否应替换为丘陵变种群系。
     *
     * @param genLayer 当前的生成层实例
     * @param original 原始的返回值（代表该点是否为丘陵群系的判定标志）
     * @return 修改后的判定值，若返回 original 表示不修改
     */
    default int onLayerHills(GenLayer genLayer, int original) { return original; }

    /**
     * 注册允许要塞（Stronghold）生成的生物群系列表。
     * <p>此方法在要塞结构生成器初始化时调用，用于提供所有可生成要塞的群系。
     * 默认原版包括主世界的大部分生物群系（沙漠、平原、森林等）。
     * <p><b>注意：</b>列表为可修改引用，直接添加或移除群系会影响要塞生成位置。
     *
     * @param original 当前允许要塞生成的生物群系列表（可修改）
     */
    default void onStrongholdAllowedRegister(List<BiomeGenBase> original) {}

    /**
     * 注册允许村庄（Village）生成的生物群系列表。
     * <p>此方法在村庄结构生成器初始化时调用，默认包括平原、沙漠等。
     * 修改此列表可控制村庄在特定群系中出现或屏蔽。
     *
     * @param original 当前允许村庄生成的生物群系列表（可修改）
     */
    default void onVillageAllowedRegister(List<BiomeGenBase> original) {}

    /**
     * 注册下界（地狱）维度中可生成的生物群系列表。
     * <p>此方法在下界生物群系初始化时调用，默认只包含地狱（Hell）群系。
     *
     * @param original 当前下界可用的生物群系列表（可修改）
     */
    default void onUnderworldBiomesRegister(List<BiomeGenBase> original) {}

    /**
     * 注册允许玩家出生点（Spawn Point）所在的生物群系列表。
     * <p>此方法在世界初始化时调用，用于确定玩家首次出生时可以落在哪些群系上。
     * 默认通常为主世界的所有非海洋、非极端地形群系。
     * 修改此列表可控制玩家出生位置的群系范围。
     *
     * @param original 当前允许玩家出生的生物群系列表（可修改）
     */
    default void onPlayerSpawnableRegister(List<BiomeGenBase> original) {}
}
```

#### IBeaconUpdateHandler

用途: 修改信标方块和信标物品是否有效。

注册示例:
```java
Handlers.BeaconUpdate.register(new IBeaconUpdateHandler() {
    @Override
    public boolean onBlockValidModify(TileEntityBeacon tileEntityBeacon, int blockID, boolean original) {
        return original;
    }
    // ...
});
```

```java
/**
 * 信标（Beacon）更新处理器接口。
 * 用于监听并修改信标方块在更新时对下方基座方块和放入物品的有效性验证逻辑。
 * 实现此接口，可在不修改原版代码的情况下调整信标可接受的方块类型或物品类型。
 * <p>所有方法均为默认方法，默认返回原始值，无需强制覆写，按需扩展即可。
 * <p><b>注意：</b>信标逻辑主要在服务端执行，因此这些方法通常仅在服务端调用。
 *
 * @see net.minecraft.tileentity.TileEntityBeacon
 * @see net.minecraft.item.ItemStack
 */
public interface IBeaconUpdateHandler {

    /**
     * 修改信标下方基座方块（即金字塔结构）的有效性判定。
     * 通过此方法可自定义允许的方块类型，例如允许下界合金块或自定义模组方块。
     *
     * @param tileEntityBeacon 当前信标方块实体对象
     * @param blockID          被检查方块的 ID（数字或注册名，取决于实现）
     * @param original         原始的判定结果（根据原版逻辑计算得出）
     * @return 修改后的判定结果（true 表示方块有效，可作为信标基座；false 表示无效），若返回 original 表示不修改
     */
    default boolean onBlockValidModify(TileEntityBeacon tileEntityBeacon, int blockID, boolean original) { return original; }

    /**
     * 修改信标槽位中放入物品的有效性判定。
     * 通过此方法可自定义允许的物品，例如添加下界合金锭或模组特有物品。
     *
     * @param tileEntityBeacon 当前信标方块实体对象
     * @param itemStack        被检查的物品堆（可能为 null 或空）
     * @param original         原始的判定结果（根据原版逻辑计算得出）
     * @return 修改后的判定结果（true 表示物品有效，可放入信标；false 表示无效），若返回 original 表示不修改
     */
    default boolean onItemValidModify(TileEntityBeacon tileEntityBeacon, ItemStack itemStack, boolean original) { return original; }
}
```

#### IBarbecueListener

用途: 扩展烧烤系统，支持自定义烧烤结果与判断逻辑。

注册示例:
```java
Handlers.Barbecue.register(new IBarbecueListener() {
    @Override
    public ItemStack getCookResult(ItemStack input) { return null; }
    // ...
});
```

```java
/**
 * 烧烤（Barbecue）逻辑监听器接口。
 * 用于自定义烧烤的烹饪结果判定。
 * 实现此接口并注册，可在不修改原版代码的情况下扩展或覆盖烧烤配方。
 * 两个方法均为默认方法，按需覆写即可。
 */
public interface IBarbecueListener {

    /**
     * 获取指定输入物品经过烧烤后得到的产物（ItemStack）。
     * <p>此方法在烧烤逻辑查询配方时调用，可用于添加自定义配方或覆盖原版配方。
     * 若返回 null，则表示此输入无法通过烧烤获得任何产物（即不处理）。
     * 若返回非空 ItemStack，则会作为烹饪结果使用（可能覆盖原版配方）。
     *
     * @param input 待烧烤的输入物品堆（可能为 null 或空，但不应该发生）
     * @return 烧烤产出的物品堆（可为空或 null），若 null 表示无自定义结果
     */
    default ItemStack getCookResult(ItemStack input) { return null; }

    /**
     * 检查给定的物品堆是否可以作为烧烤产物（即是否被此监听器识别为有效结果）。
     * <p>如果不是，则物品会被烧掉
     * 默认返回 false，表示不识别任何物品。
     *
     * @param itemStack 待检查的物品堆
     * @return true 表示该物品被识别为烧烤产物，false 表示不是
     */
    default boolean isCookResult(ItemStack itemStack) { return false; }
}
```

#### IArrowRegisterListener

用途: 注册新箭矢材料，通常用于扩展箭矢回收率等属性。

注册示例:
```java
Handlers.ArrowRegister.register(new IArrowRegisterListener() {
    @Override
    public void onRegister(Consumer<Material> registry) { }
});
```

```java
public interface IArrowRegisterListener {
    @Deprecated(since = "1.3.6")
    default void onRegister(FloatProperty<Material> registry) {}
    default/**
 * 箭头（Arrow）材料注册监听器接口。
 * 用于在箭头系统初始化时，向注册表中添加自定义的箭头材料（Material），
 * 以便游戏能够识别并处理不同材质（如铜箭、银箭、铁箭等）的箭头行为。
 * 接口提供两个重载方法，建议使用新的 {@link #onRegister(Consumer)} 方法，
 * 旧版本方法 {@link #onRegister(FloatProperty)} 已被弃用。
 *
 * @see net.minecraft.item.ItemArrow
 * @see net.minecraft.util.FloatProperty
 */
public interface IArrowRegisterListener {

    /**
     * 旧版箭头材料注册方法（已弃用）。
     * <p>该方法通过 {@link FloatProperty} 封装材料属性进行注册，但由于设计限制，
     * 在 1.3.6 版本后已被新方法取代。
     * <p><b>弃用原因：</b>FloatProperty 无法灵活扩展材料属性，且注册方式不够直观。
     *
     * @param registry 材料注册器，包含属性如速度修正、伤害修正等（以 FloatProperty 形式传递）
     * @deprecated 自 1.3.6 起弃用，请使用 {@link #onRegister(Consumer)} 替代
     */
    @Deprecated(since = "1.3.6")
    default void onRegister(FloatProperty<Material> registry) {}

    /**
     * 新版箭头材料注册方法。
     * <p>通过 {@link Consumer} 函数式接口提供材料注册器，允许直接向注册表中添加或修改
     * 箭头材料（Material）实例。使用此方法可灵活配置材料属性（如基础伤害、重量、速度等）。
     *
     * @param registry 材料注册器的消费者，可通过其 {@code accept} 方法注册自定义材料
     */
    default void onRegister(Consumer<Material> registry) {}
} void onRegister(Consumer<Material> registry) {}
}
```

#### IArmorModelListener (CLIENT only)

用途: 客户端护甲材质和模型注册，支持自定义护甲模型与贴图。

注册示例:
```java
Handlers.ArmorModel.register(new IArmorModelListener() {
    @Override
    public void onArmorModelRegister(Consumer<ModelBiped> registry) { }
    // ...
});
```

```java
/**
 * 盔甲模型与纹理监听器接口。
 * 用于自定义盔甲（ItemArmor）的渲染模型和纹理资源。
 * 实现此接口并注册，可在不修改原版代码的情况下，为特定盔甲物品替换模型或纹理，
 * 例如实现自定义盔甲外观、动态纹理或特殊渲染效果。
 * <p>所有方法均为默认方法，默认返回 null 或空实现，按需覆写即可。
 * <p><b>注意：</b>此接口中的方法仅在客户端渲染线程调用，用于盔甲的渲染准备阶段。
 *
 * @see net.minecraft.item.ItemArmor
 * @see net.minecraft.client.model.ModelBiped
 * @see net.minecraft.util.ResourceLocation
 */
public interface IArmorModelListener {

    /**
     * 获取指定盔甲物品在特定槽位（头盔、胸甲、护腿、靴子）下应使用的纹理资源。
     * <p>此方法在盔甲渲染时调用，用于决定该盔甲层使用的纹理文件路径。
     * 若返回 {@code null}，则使用原版默认纹理（或由其它监听器/系统提供）。
     * <p>典型用法：根据盔甲物品的材质、耐久或NBT数据返回不同的纹理，实现动态变色或自定义图案。
     *
     * @param itemArmor 当前要渲染的盔甲物品实例
     * @param slotIndex 盔甲槽位索引（0=头盔, 1=胸甲, 2=护腿, 3=靴子）
     * @return 纹理资源的 ResourceLocation（如 "modid:textures/models/armor/custom_layer_1.png"），
     *         或 null 表示使用默认纹理
     */
    default ResourceLocation getArmorTexture(ItemArmor itemArmor, int slotIndex) { return null; }

    /**
     * 向盔甲模型注册表中注册自定义的模型（ModelBiped）。
     * <p>此方法在盔甲系统初始化时调用，允许将自定义的模型实例添加到注册表中，
     * 以便在后续渲染时通过 {@link #getArmorModel(ItemArmor, int)} 获取。
     * <p>通常配合 {@link #getArmorModel(ItemArmor, int)} 使用：
     * 先在注册阶段将模型放入注册表，然后在渲染时根据物品返回对应的模型实例。
     *
     * @param registry 模型注册器的消费者，可通过其 {@code accept} 方法注册自定义 ModelBiped 实例
     */
    default void onArmorModelRegister(Consumer<ModelBiped> registry) {}

    /**
     * 获取指定盔甲物品在特定槽位下应使用的渲染模型（ModelBiped）。
     * <p>此方法在盔甲渲染时调用，用于决定该盔甲层使用哪个模型进行绘制。
     * 若返回 {@code null}，则使用原版默认模型（或由其它监听器/系统提供）。
     * <p>典型用法：为不同盔甲返回不同的模型（如改变形状、添加额外部件），
     * 或根据物品状态（如充能、附魔）动态切换模型。
     *
     * @param itemArmor 当前要渲染的盔甲物品实例
     * @param slotIndex 盔甲槽位索引（0=头盔, 1=胸甲, 2=护腿, 3=靴子）
     * @return 自定义的 ModelBiped 实例，或 null 表示使用默认模型
     */
    default ModelBiped getArmorModel(ItemArmor itemArmor, int slotIndex) { return null; }
}
```

#### IWorldLoadListener (CLIENT only)

用途: 客户端世界加载与卸载钩子，适用于单人模式资源重载或状态清理。

注册示例:
```java
Handlers.WorldLoad.register(new IWorldLoadListener() {
    @Override
    public void onWorldLoad(WorldClient world) { }
});
```

```java
/**
 * 世界（World）加载与卸载事件监听器接口。
 * 用于监听客户端世界（WorldClient）的加载和卸载事件。
 * <p>典型使用场景：在世界加载时初始化客户端资源（如小地图数据、自定义渲染器），
 * 在世界卸载时清理资源（如释放缓存、取消网络监听）。
 * <p><b>注意：</b>此接口中的方法仅在客户端环境调用，服务端不会触发。
 * 所有方法均为默认方法，默认空实现，按需覆写即可。
 *
 * @see net.minecraft.client.multiplayer.WorldClient
 */
public interface IWorldLoadListener {

    /**
     * 当客户端世界被卸载时触发。
     * <p>此事件在玩家离开世界、断开服务器连接或切换维度时发生，
     * 此时 WorldClient 对象即将被销毁。
     * 可用于执行清理操作，如移除渲染器、保存临时状态等。
     *
     * @param worldBefore 即将被卸载的 WorldClient 对象，可用于获取当前世界信息
     */
    default void onWorldUnload(WorldClient worldBefore) {}

    /**
     * 当客户端世界被加载时触发。
     * <p>此事件在玩家进入世界、维度切换完成或重连成功时发生，
     * 此时 WorldClient 对象已创建并初始化。
     * 可用于执行初始化操作，如注册渲染器、加载自定义数据等。
     *
     * @param world 新加载的 WorldClient 对象，可用于访问世界信息
     */
    default void onWorldLoad(WorldClient world) {}
}
```

#### IWorldInfoListener

用途: 读取/写入 `level.dat` 时的 NBT 扩展钩子。

注册示例:
```java
Handlers.WorldInfo.register(new IWorldInfoListener() {
    @Override
    public void onNBTWrite(NBTTagCompound nbt) { }
    @Override
    public void onNBTRead(NBTTagCompound nbt) { }
});
```

```java
/**
 * 世界信息（WorldInfo）NBT 数据读写事件监听器接口。
 * 用于监听世界信息对象在序列化（写入 NBT）和反序列化（读取 NBT）时的回调，
 * 允许在保存或加载世界数据时，添加、修改或读取自定义的世界级别数据（如额外维度属性、全局统计等）。
 * <p>典型应用：模组需要存储与世界相关的全局数据（而非玩家或区块数据），
 * 可在世界保存时写入自定义标签，在世界加载时读取恢复。
 * <p><b>注意：</b>此接口中的方法通常在服务端保存/加载世界时调用，
 * 但在客户端（如单机）也会触发（因为内置服务端与客户端共享 WorldInfo）。
 * 具体调用环境取决于世界数据的同步方式。
 *
 * @see net.minecraft.world.storage.WorldInfo
 * @see net.minecraft.nbt.NBTTagCompound
 */
public interface IWorldInfoListener {

    /**
     * 当世界信息即将写入 NBT（保存）时触发。
     * <p>此方法在世界保存过程中调用，此时 NBT 标签对象已创建，
     * 可向其中添加自定义数据（如模组版本、自定义规则等）。
     * <p><b>注意：</b>修改 NBT 时请注意键名唯一性，避免覆盖原版数据。
     *
     * @param nbt 用于存储世界信息的 NBTTagCompound 对象（可修改），
     *            可通过 {@link NBTTagCompound#setTag} 等方法添加自定义数据
     */
    default void onNBTWrite(NBTTagCompound nbt) {}

    /**
     * 当世界信息从 NBT 读取（加载）时触发。
     * <p>此方法在世界加载过程中调用，此时 NBT 标签对象已包含从磁盘读取的数据，
     * 可用于提取之前保存的自定义数据，并应用到世界状态中。
     * <p><b>注意：</b>读取时建议使用 {@link NBTTagCompound#hasKey} 检查键是否存在，
     * 以兼容旧版本或未保存自定义数据的世界。
     *
     * @param nbt 包含世界信息的 NBTTagCompound 对象（只读），
     *            可通过 {@link NBTTagCompound#getTag} 等方法获取自定义数据
     */
    default void onNBTRead(NBTTagCompound nbt) {}
}
```

#### TimedTaskHandler

```java
// TimedTaskHandler 不使用 Listener 接口模式，通过 registerTimedTask 直接注册。
// 注册方式: Handlers.TimedTask.registerTimedTask(int ticks, BooleanSupplier condition, Runnable task)
//   ticks: 延迟 tick 数（倒计时结束后开始检查 condition）
//   condition: 每 tick 检查，返回 true 则执行 task 并移除该定时任务
//   task: 要执行的任务体
// 内部维护 List<TimedTask>，每 tick 调用 onTick() 遍历并执行到期任务。
```

---

### 1.8 事件对象 (Events)

事件对象位于 `moddedmite.rustedironcore.api.event.events` 包中，既包含 Java `record`，也包含普通封装类：

| 事件 | 关键字段 / 方法 | 用途 |
|---|---|---|
| `PlayerLoggedInEvent` | `record(ServerPlayer player, boolean firstLogin)` | 玩家登录 |
| `PlayerLoggedOutEvent` | `record(ServerPlayer player)` | 玩家登出 |
| `PlayerRespawnEvent` | `record(ServerPlayer player, Dimension dimension)` | 玩家重生 |
| `PlayerCrossDimensionEvent` | `record(ServerPlayer player, Dimension from, Dimension to)` | 跨维度 |
| `CraftingRecipeRegisterEvent` | `getVanilla()`, `getShaped()`, `getShapeless()`, `registerShapedRecipe(...)`, `registerShapelessRecipe(...)`, `registerArmorRepairRecipe(...)` | 注册或修改合成配方 |
| `CraftingRecipeRegisterEvent.RecipeArgs` | `extendsNBT()`, `difficulty(float)`, `skillSet(int)`, `skillSet(int[])`, `allowDamaged()`, `consumeRule(...)`, `keepQuality()` | 配方构建器，支持 NBT、难度、技能、损伤、消耗规则 |
| `CraftingRecipeRegisterEvent.ConsumeRule` | `matches(ItemStack)`, `apply(ItemStack)` | 物品消耗规则 |
| `SmeltingRecipeRegisterEvent` | `register(...)`, `registerSpecial(...)`, `registerSpecial(..., boolean)` | 注册熔炼配方 |
| `TradingRegisterEvent` | `registerProfession(...)`, `registerVillagerStock(...)`, `registerBlackSmithSelling(...)` | 注册村民职业与交易 |
| `DimensionRegisterEvent` | `record(DimensionHandler handler)`, `getNextDimensionID()`, `register(Dimension, DimensionContext)` | 注册维度 |
| `CommandRegisterEvent` | `register(ICommand command)` | 注册命令 |
| `EntityTrackerRegisterEvent` | `registerEntityTracker(Predicate<Entity>, int, int)`, `registerEntityTracker(Predicate<Entity>, int, int, boolean)`, `registerEntityPacket(Predicate<Entity>, EntityTrackerHandler.EntitySupplier)`, `registerEntityPacket(Predicate<Entity>, int, Function<Entity, Packet>, EntityTrackerHandler.EntitySupplier)`, `registerEntityPacket(Predicate<Entity>, Function<Entity, moddedmite.rustedironcore.network.Packet>)` | 注册实体追踪器与实体同步包 |
| `OreGenerationRegisterEvent` | `record(OreGenerationHandler handler)`, `register(Dimension, WorldGenMinable, int)`, `register(Dimension, WorldGenMinable, int, boolean)`, `unregister(Dimension, int)` | 注册矿石生成 |
| `BiomeDecorationRegisterEvent` | `getMap()`, `register(Dimension, WorldGenerator)`, `register(Dimension, WorldGenerator, int)` | 注册群系装饰 |
| `TileEntityDataTypeRegisterEvent` | `register(Class<? extends TileEntity>)`, `register(int, Class<? extends TileEntity>)` | 注册 TileEntity 数据类型 |
| `StructureRegisterEvent` | `register(Dimension, MapGenStructure)` (已弃用) | 注册结构 (弃用，推荐使用 `MapGenRegisterEvent` ) |
| `SpawnConditionRegisterEvent` | `register(Class<? extends Entity>, SpawnCondition)` | 注册实体生成条件 |
| `StructureNBTRegisterEvent` | `registerStructureStart(...)`, `registerStructureComponent(...)` | 注册结构 NBT |
| `MapGenRegisterEvent` | `register(Dimension, MapGenBase)`, `registerStructure(Dimension, MapGenStructure)` | 注册 MapGen 基础元素 / 结构 |

---

### 1.9 事件系统底层机制

核心类均在 `huix.glacier.event` 包下，源自 Legacy Fabric API 设计：

```
Event<T> (抽象)
  ├── volatile T invoker    // 合并所有监听器的单一调用实例
  ├── T invoker()           // 获取 invoker
  └── void register(T listener)

EventFactory
  ├── createArrayBacked(type, invokerFactory)              // 创建数组支撑事件
  ├── createArrayBacked(type, emptyInvoker, invokerFactory) // 带空调用优化
  └── invalidate()                                          // 重建所有事件 invoker

ArrayBackedEvent<T> implements Event<T>
  ├── T[] handlers         // 监听器数组
  ├── ReentrantLock        // 线程安全
  └── 优化: 1个监听器时直接使用监听器本身作为 invoker
```

---

## 2. 注册系统 (Registry)

### 2.1 MinecraftRegistry — 方块/物品注册

**全限定名**: `huix.glacier.api.registry.MinecraftRegistry`

```java
public class MinecraftRegistry implements IRegistryInstance {
    // 构造
    public MinecraftRegistry(String modName);

    // 全局实例（最后一次创建的那个）
    public static MinecraftRegistry instance;

    // 是否启用自动物品注册
    public boolean autoItemRegister;

    // 注册方块（自动设置 namespace、贴图名、创建 ItemBlock）
    public void registerBlock(Block block, String resource, String name);
    public void registerBlock(Block block, String resourceLocation);

    // 注册铁砧方块（创建 ItemAnvilBlock）
    public void registerBlockAnvil(BlockAnvil block, String resource, String name);
    public void registerBlockAnvil(BlockAnvil block, String resourceLocation);

    // 注册物品
    public Item registerItem(String resource, String name, Item item);
    public Item registerItem(String resource, Item item);

    // 旧的物品热值注册方法，建议改用属性系统
    @Deprecated(since = "1.3.9")
    public void registerItemHeatLevel(int itemId, int heatLevel);

    // 启用自动物品注册（后续创建 GlacierItem 子类时无需手动注册）
    public MinecraftRegistry initAutoItemRegister();

    // 获取 mod 命名空间
    public String getNameSpace();

    // 物品热值映射表
    public HashMap&lt;Integer, Integer&gt; itemHeatLevelMap;

    // 已弃用 @1.3.9，改用 ItemProperties.HeatLevel 属性系统
    @Deprecated
    public void registerItemHeatLevel(int itemId, int heatLevel);
}
```

**使用示例**:

```java
class MyRegistry implements IGameRegistry {
    @Override
    public void onGameRegistry() {
        MinecraftRegistry registry = new MinecraftRegistry("spireforge");
        registry.initAutoItemRegister();

        registry.registerBlock(myBlock, "my_block", "我的方块");
        registry.registerItem("my_item", "我的物品", myItem);
    }
}
```

---

### 2.2 IGameRegistry — 入口点

**全限定名**: `huix.glacier.api.entrypoint.IGameRegistry`

```java
@FunctionalInterface
public interface IGameRegistry {
    void onGameRegistry();
}
```

在 `fml.mod.json` 中声明：

```json
{
  "entrypoints": {
    "registry": "com.spireforge.registry.SpireRegistry"
  }
}
```

---

### 2.3 属性注册 (Property System)

**全限定名**: `moddedmite.rustedironcore.property.MaterialProperties` / `ItemProperties`

属性系统允许为 `Material` 或 `Item` 注册自定义 key-value 属性。必须在 `Handlers.PropertiesRegistry` 回调中注册。

```java
// Material 属性（预定义常量）
MaterialProperties.RepairItem        // Property<Material, Item>      修复物品
MaterialProperties.HarvestEfficiency // FloatProperty<Material>       采集效率
MaterialProperties.PeerCoin          // Property<Material, ItemCoin>  对应硬币
MaterialProperties.PeerCoinXP        // IntegerProperty<Material>     硬币经验值
MaterialProperties.BucketMeltingChance // FloatProperty<Material>     桶熔化概率

// Item 属性（预定义常量）
ItemProperties.RockExperience        // IntegerProperty<Item>         宝石经验
ItemProperties.HeatLevel             // IntegerProperty<Item>         热值等级
ItemProperties.HeatLevelRequired     // IntegerProperty<Item>         所需热值等级
ItemProperties.BurnTime              // IntegerProperty<Item>         燃烧时间
ItemProperties.CraftConsumeOverride  // Property<Item, UnaryOperator<ItemStack>> 合成消耗覆盖
```

**自定义属性**:

```java
// 创建自定义属性
BooleanProperty<Material> IS_MAGIC = BooleanProperty.of("is_magic", false);

// 在 PropertiesRegistry 中注册
Handlers.PropertiesRegistry.register(() -> {
    IS_MAGIC.register(myMaterial, true);
});

// 读取
boolean magic = IS_MAGIC.getOrDefault(myMaterial);
```

---

## 3. 方块与物品基类

### 3.1 方块基类 (api/block)

| 类 | 全限定名 | 继承 | 用途 |
|---|---|---|---|
| `WallBlock` | `...api.block.WallBlock` | `BlockWall` | 墙方块，支持任意方块纹理 |
| `StairsBlock` | `...api.block.StairsBlock` | `BlockStairs` | 楼梯方块 |
| `PaneBlock` | `...api.block.PaneBlock` | `BlockPane` | 玻璃板/铁栏杆类 |
| `WorkbenchBlock` | `...api.block.WorkbenchBlock` | `BlockWorkbench` | 工作台，自定义速度修正 |
| `DoorBlock` | `...api.block.DoorBlock` | `BlockDoor` | 门方块 |

**WallBlock 构造**:

```java
public WallBlock(int id, Block baseBlock);
```

**StairsBlock 构造**:

```java
public StairsBlock(int id, Block baseBlock, int textureSide);
```

**PaneBlock 构造**:

```java
public PaneBlock(int id, String sideTexture, String topTexture,
    Material material, boolean canDrop);
```

**WorkbenchBlock 构造**:

```java
public WorkbenchBlock(int id, Material material,
    float speedModifier,          // 合成速度修正（1.0=正常）
    Material checkAgainst)        // 工具硬度检查基准材质
```

**DoorBlock 构造**:

```java
public DoorBlock(int id, Material material, Supplier<Item> doorItem);
// 内部通过 Supplier 延迟获取门物品，避免初始化时序问题
```

---

### 3.2 物品基类 (api/item)

| 类 | 全限定名 | 继承 | 用途 |
|---|---|---|---|
| `NuggetItem` | `...api.item.NuggetItem` | `ItemNugget` | 粒物品，自动维护 Material→ItemNugget 映射 |
| `IngotItem` | `...api.item.IngotItem` | `ItemIngot` | 锭物品 |
| `FishingRodItem` | `...api.item.FishingRodItem` | `ItemFishingRod` | 钓鱼竿，按材质换贴图 |
| `BowItem` | `...api.item.BowItem` | `ItemBow` | 弓 (@Deprecated(since = "1.3.5")) |
| `DoorItem` | `...api.item.DoorItem` | `ItemDoor` | 门物品，`Supplier<BlockDoor>` 延迟关联 |

**NuggetItem 构造**:

```java
public NuggetItem(int id, Material material);
```

**IngotItem 构造**:

```java
public IngotItem(int id, Material material);
```

**FishingRodItem 构造**:

```java
public FishingRodItem(int id, Material hook_material)
// 自动注册 simpleRecipe 和 material→ItemFishingRod 映射
```

**BowItem 构造**:

```java
@Deprecated(since = "1.3.5")
public BowItem(int id, Material reinforcement_material,
    int maxDamage, int velocityBonusPercentage);
```

**DoorItem 构造**:

```java
public DoorItem(int id, Material material, Supplier<BlockDoor> blockDoor);
// 内部通过 Supplier 延迟获取 BlockDoor，避免初始化时序问题
```

---

### 3.3 材料接口 (extension/material)

所有材料接口在 `huix.glacier.api.extension.material` 包下：

| 接口 | 方法 | 用途 |
|---|---|---|
| `IEquipmentMaterial` | `getName()`, `getDurability()`, `getEnchantability()`, `getMaxQuality()`, `getDamageVsEntity()` | 装备材料核心 |
| `IToolMaterial` | `getHarvestEfficiency()` | 工具采集效率 |
| `IArmorMaterial` | `getProtection()`, `getLossOfChainMail()` | 护甲防护值, 锁链甲耐久损失(默认2) |
| `IRepairableMaterial` | `getRepairItem()` | 修复物品 |
| `IComboMaterial` | `getNugget()`, `getIngot()` | 粒/锭关联 |
| `IBucketMaterial` | `getMeltingChance()` | 桶熔化概率 |
| `ICoinMaterial` | `getExperienceValue()`, `getForInstance()`, `getNuggetPeer()` | 硬币材料 |
| `IArrowMaterial` | `getChanceOfRecovery()` | 箭矢回收率 |
| `IBowMaterial` | `velocityBonus()`, `maxDamage()` | 弓属性 |

**GlacierMaterial** — 从 `IEquipmentMaterial` 构造 `Material`:

```java
public class GlacierMaterial extends Material {
    public GlacierMaterial(IEquipmentMaterial equipmentMaterial);
    public float getDamageVsEntity();
}
```

---

### 3.4 物品扩展接口 (extension/item)

在 `huix.glacier.api.extension.item` 包下：

| 接口 | 方法 | 用途 |
|---|---|---|
| `IFusibleItem` | `getHeatLevelRequired()`, `Optional&lt;IFusibleItem&gt; cast(Item)` | 可熔炼物品（热量等级） |
| `IFuelItem` | `getHeatLevel()`, `getBurnTime()`, `Optional&lt;IFuelItem&gt; cast(Item)` | 燃料物品 |
| `IRetainableItem` | `getItemStackAfterCrafting(original)` | 合成后保留（如桶） |
| `IRockItem` | `getExperienceValueWhenSacrificed()` | 献祭经验值 |

**GlacierItem** — 自动注册物品基类:

```java
public class GlacierItem extends Item {
    public GlacierItem(MinecraftRegistry registryInstance, int par1, String texture, int num_subtypes);
    public GlacierItem(MinecraftRegistry registryInstance, int id, String texture);
    public GlacierItem(MinecraftRegistry registryInstance, int id, Material[] material_array, String texture);
    public GlacierItem(MinecraftRegistry registryInstance, int id, Material material, String texture);
    // 需配合 MinecraftRegistry.initAutoItemRegister() 使用
    // 构造时自动设置 namespace
}
```

---

## 4. 网络系统 (Network)

**包**: `moddedmite.rustedironcore.network`

### 核心接口

```java
// 数据包接口
public interface Packet {
    void write(PacketByteBuf packetByteBuf);          // 序列化
    void apply(EntityPlayer entityPlayer);            // 接收端处理
    ResourceLocation getChannel();                     // 频道标识
    Packet250CustomPayload toVanilla();      // 转为原版包（接口提供默认实现）
}

// 数据包构造器（反序列化）
@FunctionalInterface
public interface PacketSupplier {
    Packet readPacket(PacketByteBuf buf);
}

// 字节缓冲（封装 DataInputStream/DataOutputStream）
public interface PacketByteBuf {
    DataInputStream getInputStream();
    DataOutputStream getOutputStream();

    byte readByte();
    short readShort();
    ItemStack readItemStack();
    String readString();
    int readInt();
    int readVarInt();
    boolean readBoolean();
    float readFloat();
    MerchantRecipeList readMerchantRecipeList();
    double readDouble();
    long readLong();
    String readUTF();
    int readUnsignedByte();
    int readUnsignedShort();
    int read(byte[] b);
    int read(byte[] b, int off, int len);
    void readFully(byte[] b);
    void readFully(byte[] b, int off, int len);

    void writeByte(int paramInt);
    void writeShort(int paramInt);
    void writeItemStack(ItemStack paramItemStack);
    void writeString(String paramString);
    void writeInt(int paramInt);
    void writeVarInt(int paramInt);
    void writeBoolean(boolean paramBoolean);
    void writeFloat(float paramFloat);
    void writeMerchantRecipeList(MerchantRecipeList list);
    void writeDouble(double paramDouble);
    void writeLong(long paramLong);
    void writeUTF(String paramUTF);
    void write(int b);
    void write(byte[] b, int off, int len);

    static PacketByteBuf out(DataOutputStream out);     // 只写
    static PacketByteBuf in(DataInputStream in);        // 只读
}
```

### 发送与注册

```java
// 注册包读取器
PacketReader.registerServerPacketReader(
    new ResourceLocation("spireforge", "my_packet"),
    buf -> new MyPacket(buf)
);
PacketReader.registerClientPacketReader(
    new ResourceLocation("spireforge", "my_packet"),
    buf -> new MyPacket(buf)
);

// 发送
// 服务端 → 指定客户端
Network.sendToClient(serverPlayer, packet);          // SERVER only
// 服务端 → 所有玩家
Network.sendToAllPlayers(packet);                    // SERVER only
// 客户端 → 服务端
Network.sendToServer(packet);                        // CLIENT only
```

> 如果在服务器端调用 `Network.sendToServer(packet)`，将抛出 `IllegalCallerException`。

**完整示例**:

```java
class MyPacket implements Packet {
    private final int value;

    public MyPacket(int value) { this.value = value; }
    public MyPacket(PacketByteBuf buf) { this.value = buf.readInt(); }

    @Override public void write(PacketByteBuf buf) { buf.writeInt(value); }
    @Override public void apply(EntityPlayer player) { /* 处理 */ }
    @Override public ResourceLocation getChannel() {
        return new ResourceLocation("spireforge", "my_packet");
    }
}

// 注册
PacketReader.registerServerPacketReader(
    new ResourceLocation("spireforge", "my_packet"), MyPacket::new
);
```

---

## 5. 工具类

### 5.1 Accessor — 反射工具

**全限定名**: `moddedmite.rustedironcore.api.accessor.Accessor`

```java
public final class Accessor {
    // 设置实例字段（自动移除 final 修饰符）
    public static <T, Y> void modify(Field field, T target, Y value);

    // 设置静态字段
    public static <T> void modifyStatic(Field field, T value);

    // 读取实例字段
    public static <T, Y> Y access(Field field, T target);

    // 读取静态字段
    public static <Y> Y accessStatic(Field field);

    // 调用实例方法
    public static <T, Y> Y invoke(Method method, T target, Object... args);

    // 调用静态方法
    public static <Y> Y invokeStatic(Method method, Object... args);

    // 反射创建实例
    public static <T> T createInstance(Class<T> clazz, Object... args);

    // 加载类
    public static Class<?> accessClass(String className);

    // 访问内部类
    public static Class<?> accessInnerClass(Class<?> outer, String innerName);
}
```

---

### 5.2 IdUtilExtra — ID 分配

**全限定名**: `moddedmite.rustedironcore.api.util.IdUtilExtra` (extends FML `IdUtil`)

```java
public class IdUtilExtra extends IdUtil {
    static int getNextGameTypeID();          // 游戏模式 ID
    static int getNextVillagerProfessionID();// 村民职业 ID
    static int getNextPacket23Type();        // Packet23 类型 ID
    static int getNextTileEntityDataType();  // TileEntity 数据类型 ID
    static int getNextDimensionID();         // 维度 ID
    static int getNextWorldType();           // 世界类型 ID
    static int getNextSlabGroup();           // 台阶组 ID
    static int getNextCreativeID();          // 创造标签页 ID
}
```

---

### 5.3 FabricUtil — 模组元数据

**全限定名**: `moddedmite.rustedironcore.api.util.FabricUtil`

```java
public class FabricUtil {
    static Optional<ModContainer> getModContainer(String modid);
    static Optional<ModMetadata> getModMetadata(String modid);
    static Optional<Version> getModVersion(String modid);
    static int compareModVersion(String modid, String version);
    static boolean isModLoaded(String modid);
    static boolean isServer();
    static Path getGameDirectory();
    static Path getModsDirectory();
    static Path getConfigDirectory();
    static boolean isDevelopmentEnvironment();
}
```

---

### 5.4 RandomUtil — 加权随机

**全限定名**: `moddedmite.rustedironcore.random.RandomUtil`

```java
public class RandomUtil {
    public static <T extends IntegerWeightedEntry> T getRandomEntry(List<T> list, Random random);
    public static <T extends IntegerWeightedEntry> int getTotalWeight(List<T> list);

    public static <T extends FloatWeightedEntry> T getRandomEntryFloat(List<T> list, Random random);
    public static <T extends FloatWeightedEntry> float getTotalWeightFloat(List<T> list);

    public static <T> T getRandom(List<T> list, Random random);
}
```

---

### 5.5 其他工具

| 类 | 全限定名 | 关键方法 |
|---|---|---|
| `VesselUtil` | `...api.util.VesselUtil` | `getBucket(material, contents)`, `registerBucket(...)`, `getBowl(...)`, `registerBowl(...)` |
| `PotionExtend` | `...api.util.PotionExtend` | 继承 `Potion` + 实现 `IPotion`，支持独立纹理 |
| `LogUtil` | `...api.util.LogUtil` | `getLogger()` (caller-sensitive), `configureRootLoggingLevel(Level)` |
| `GuiUtil` | `...api.util.GuiUtil` | `setWindowTitle(String)`, `getWindowTitle()` |
| `StringUtil` | `...api.util.StringUtil` | `translate(key)`, `translateF(key, args)`, `getCurrentLanguage()` |
| `ItemUtil` | `...api.util.ItemUtil` | `getNuggetForMaterial(Material)` |
| `BiomeSpawnUtil` | `...api.util.BiomeSpawnUtil` | `addSpawn(entityClass, prob, min, max, type, biomes...)`, `removeSpawn(...)`, `clearSpawn(...)` |
| `Platform` | `...api.util.Platform` | `isExperimental()` |

---

## 6. 玩家 API

**包**: `moddedmite.rustedironcore.api.player`

```java
// Player 扩展接口
public interface PlayerAPI {
    boolean ric$IsFirstLogin();
    void ric$SetFirstLogin(boolean firstLogin);
}

// ServerPlayer 扩展接口
public interface ServerPlayerAPI {
    int ric$GetNutritionLimit();
}

// ClientPlayer 扩展接口
public interface ClientPlayerAPI {
    default int ric$GetPhytonutrients();
    default void ric$SetPhytonutrients(int phytonutrients);
    default int ric$GetProtein();
    default void ric$SetProtein(int protein);
    default int ric$GetEssentialFats();
    default void ric$SetEssentialFats(int essential_fats);
    static int getPhytonutrients(ClientPlayer player);
    static int getProtein(ClientPlayer player);
    static int getEssentialFats(ClientPlayer player);
    default int ric$GetNutritionLimit();
    default void ric$SetNutritionLimit(int nutrition_limit);
    static int getNutritionLimit(ClientPlayer player);
}
```

> 这些接口通过 Mixin 注入到对应原版类中（`fml.mod.json` 的 `injectedInterface` 配置），调用时直接强制转换即可：`((ClientPlayerAPI) clientPlayer).ric$GetNutritionLimit()`

---

## 7. 世界/维度 API

**包**: `moddedmite.rustedironcore.api.world`

```java
// 维度标识 (record)
public record Dimension(String name, int id) {
    public static Dimension register(String name, int id);
    public static Dimension register(Dimension dimension);
    @Nullable public static Dimension fromString(String name);
    @Nullable public static Dimension fromId(int id);
    @Nullable public static Dimension fromWorld(World world);
    public static Stream<Dimension> streamDimensions();

    public boolean isOf(World world);
    public String toString();

    // 预定义
    public static final Dimension OVERWORLD  = register("overworld", 0);
    public static final Dimension NETHER     = register("the_nether", -1);
    public static final Dimension END        = register("the_end", 1);
    public static final Dimension UNDERWORLD = register("the_end", -2);
}

// 维度参数 (record)
public record DimensionContext(
    Supplier<WorldProvider> worldProviderFactory,
    boolean hasSkyLight,
    boolean hasCeiling
) {
}

// 矿石生成
public class MinableWorldGen extends WorldGenMinable {
    public MinableWorldGen(int minableBlockId, int numberOfBlocks);
    public MinableWorldGen(int minableBlockId, int numberOfBlocks, int blockToReplace);

    public MinableWorldGen setMaxVeinHeight(VeinHeightSupplier maxVeinHeight);
    public MinableWorldGen setMaxVeinHeight(int height);
    public int getMaxVeinHeight(World world);
    public MinableWorldGen setMinVeinHeight(VeinHeightSupplier minVeinHeight);
    public MinableWorldGen setMinVeinHeight(int height);
    public int getMinVeinHeight(World world);
    public MinableWorldGen setRandomVeinHeight(RandomVeinHeightSupplier supplier);
    public int getRandomVeinHeight(World world, Random rand);

    public static final VeinHeightSupplier BOTTOM_HEIGHT;
    public static final VeinHeightSupplier ROOF_HEIGHT;
    public static final RandomVeinHeightSupplier STANDARD_RANDOM_HEIGHT;
    @Deprecated public static final RandomVeinHeightSupplier Common;
    public static final RandomVeinHeightSupplier NETHER_RANDOM_HEIGHT;

    @FunctionalInterface
    public interface VeinHeightSupplier {
        int getVeinHeight(World world, MinableWorldGen minable);
    }

    @FunctionalInterface
    public interface RandomVeinHeightSupplier {
        int getVeinHeight(World world, Random rand, MinableWorldGen minable);
    }
}

// 生物群系 API
public interface BiomeAPI {
    default String getBiomeUnlocalizedName();
    default String setBiomeUnlocalizedName(String unlocalizedName);
    static String getBiomeUnlocalizedName(BiomeGenBase biome);
}
```

---

## 8. 模型与渲染 API

**包**: `moddedmite.rustedironcore.api.model` / `moddedmite.rustedironcore.api.render`

```java
// JSON 模型注册入口
public interface JsonModelRegistry {
    void registerBlockModel(Block block, String modelId);
    void registerBlockModel(Block block, Identifier modelId);
    void registerBlockModel(Block block, int metadata, String modelId);
    void registerBlockModel(Block block, int metadata, Identifier modelId);
    void registerBlockState(Block block, String blockStateId);
    void registerBlockState(Block block, Identifier blockStateId);
    void registerBlockState(Block block, int metadata, String blockStateId);
    void registerBlockState(Block block, int metadata, Identifier blockStateId);
    void registerBlockStateProperties(Block block, JsonBlockStatePropertyResolver resolver);
    void registerItemModel(Item item, String modelId);
    void registerItemModel(Item item, Identifier modelId);
    void registerItemModel(Block block, String modelId);
    void registerItemModel(Block block, Identifier modelId);
    void registerItemModel(Item item, int metadata, String modelId);
    void registerItemModel(Item item, int metadata, Identifier modelId);
    void registerItemModel(Block block, int metadata, String modelId);
    void registerItemModel(Block block, int metadata, Identifier modelId);
    void registerBuiltInRenderer(Item item, BuiltInEntityRenderer renderer);
    void registerBuiltInRenderer(Item item, int metadata, BuiltInEntityRenderer renderer);
    void registerBuiltInRenderer(Block block, BuiltInEntityRenderer renderer);
}

// 全局模型管理器 (单例)
public final class JsonBlockModelManager implements ResourceManagerReloadListener, JsonModelRegistry {
    public static final JsonBlockModelManager INSTANCE;
    public boolean hasJsonBlockInRenderType(int renderType);
    public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z, int metadata);
    public boolean renderBlockAsItem(Block block, int metadata, float brightness);
    public boolean renderBlockAsItem(Block block, int metadata, float brightness, JsonBlockModel.RenderContext context);
    public boolean renderItem(ItemStack stack, Icon fallbackIcon, JsonBlockModel.RenderContext context);
    public boolean renderItemIntoGui(ItemStack stack, int x, int y, float zLevel);
    public java.util.Optional<JsonBlockModel> getItemModel(ItemStack itemStack);
    public java.util.Optional<JsonBlockModel> getModel(Block block, int metadata);
    public void registerTextureAtlasSprites(TextureMap textureMap);
}

// 自定义实体渲染器
@Environment(EnvType.CLIENT)
public interface BuiltInEntityRenderer {
    void render(ItemStack itemStack, JsonBlockModel.RenderContext context);
}

// 渲染上下文管理
public class JsonModelRenderContext {
    public static void push(JsonBlockModel.RenderContext context);
    public static void pop();
    public static JsonBlockModel.RenderContext currentOr(JsonBlockModel.RenderContext fallback);
}

// Render API
public interface RenderAPI {
    default boolean forceGlowOverride() { return false; }
}
```

---

## 9. 村民交易 API

**包**: `moddedmite.rustedironcore.villager`

```java
// 交易注册事件
public record TradingRegisterEvent(Map<Integer, Tuple> villagerStockList, Map<Integer, Tuple> blacksmithSellingList) {
    public VillagerSettings registerProfession(ResourceLocation texture);
    public VillagerSettings registerProfession(int profession, ResourceLocation texture);
    public VillagerSettings registerProfession(int profession, String name, ResourceLocation texture);
    public VillagerSettings registerProfession(int profession, VillagerSettings settings);
    public Optional<VillagerSettings> getForProfession(int profession);
    public void registerVillagerStock(Item item, int minCount, int maxCount);
    @Deprecated(since = "1.3.6")
    public void registerVillagerStock(int id, Tuple data);
    public void registerBlackSmithSelling(Item item, int minCount, int maxCount);
    @Deprecated(since = "1.3.6")
    public void registerBlackSmithSelling(int id, Tuple data);
}

// 交易配方 (函数式接口)
@FunctionalInterface
public interface RecipeEntry {
    void apply(MerchantRecipeList list, EntityVillager villager, Random random);
}

// 购买条目: 玩家卖出物品，获得绿宝石
public record BuyEntry(int id, float originalProbability) implements RecipeEntry {
    @Override
    public void apply(MerchantRecipeList list, EntityVillager villager, Random random);
}

// 出售条目: 玩家付绿宝石，获得物品
public record SellEntry(int id, float originalProbability) implements RecipeEntry {
    @Override
    public void apply(MerchantRecipeList list, EntityVillager villager, Random random);
}

// 村民职业配置
public class VillagerSettings {
    public int getProfession();
    public String getName();
    public String getTranslatedName();
    public ResourceLocation getTexture();
    public List<RecipeEntry> getRecipeEntries();

    public static final ResourceLocation FoolTexture;
    public static final ResourceLocation FarmerTexture;
    public static final ResourceLocation LibrarianTexture;
    public static final ResourceLocation PriestTexture;
    public static final ResourceLocation SmithTexture;
    public static final ResourceLocation ButcherTexture;

    public VillagerSettings buyEntry(int id, float originalProbability);
    public VillagerSettings sellEntry(int id, float originalProbability);
    public VillagerSettings addEntry(RecipeEntry entry);
    public void removeEntry(RecipeEntry entry);
    public void removeBuyEntryForId(int id);
    public void removeSellEntryForId(int id);
    public VillagerSettings setBanned(boolean banned);
    public boolean isBanned();
}
```

---

## 10. 键位绑定与本地化

**包**: `moddedmite.rustedironcore.localization` (LocalizationEnum) / `moddedmite.rustedironcore.keybinding` (KeyBindingExtra)

```java
// 扩展键位绑定
public class KeyBindingExtra extends KeyBinding {
    public KeyBindingExtra(String keyDescription, int keyCode, String category);
    public int getKeyCodeDefault();
    public String getKeyCategory();
    public String getKeyDescription();
    public int getKeyCode();
    public void setKeyCode(int keyCode);
    public int compareTo(KeyBindingExtra keyBindingExtra);
}

// 客户端键位注册监听器
@Environment(EnvType.CLIENT)
public interface IKeybindingListener {
    default void onKeybindingRegister(Consumer<KeyBinding> registry) {}
}

// JSON 模型注册监听器
@Environment(EnvType.CLIENT)
public interface IJsonModelListener {
    default void onJsonModelRegister(JsonModelRegistry registry) {}
}

// 本地化枚举接口
public interface LocalizationEnum {
    String getKey();                            // 翻译键
    default String translate();                 // 翻译（无参数）
    default String translate(Object... params); // 翻译（带参数）
}

// 使用示例
enum MyText implements LocalizationEnum {
    FORGE_SUCCESS,
    FORGE_FAIL;

    @Override
    public String getKey() {
        return "spireforge." + name().toLowerCase();
    }
}

// MyText.FORGE_SUCCESS.translate() → 从 lang 文件读取 spireforge.forge_success
```

---

## 11. 常用 Mixin 接口

**全限定名**: `moddedmite.rustedironcore.api.interfaces`

```java
// Potion 扩展接口（Mixin 注入 Potion 类）
public interface IPotion {
    default boolean ric$UsesIndividualTexture() { return false; }
    ResourceLocation ric$GetTexture();
}

// 配方扩展接口（Mixin 注入 ShapedRecipes/ShapelessRecipes）
public interface IRecipeExtend {
    void ric$SetAllowDamaged(boolean allow);
    boolean ric$AllowDamaged();
    void ric$SetConsumeRules(List<ConsumeRule> rules);
    List<ConsumeRule> ric$GetConsumeRules();
    void ric$SetKeepQuality();
}
```

---

## 附录 A：API 快速查找表

### A.1 事件系统 (Event System)

| 需求 | Handler | Listener 接口 |
|---|---|---|
| 监听玩家登录/登出/重生 | `Handlers.PlayerEvent` | `IPlayerEventListener` |
| 修改玩家属性上限 | `Handlers.PlayerAttribute` | `IPlayerAttributeListener` |
| 监听实体攻击/受伤 | `Handlers.Combat` | `ICombatListener` |
| 监听实体死亡/生成 | `Handlers.EntityEvent` | `IEntityEventListener` |
| 监听实体更新 (Tick) | `Handlers.Tick` | `ITickListener` |
| 实体怪物 NBT 读写 | `Handlers.EntityMobMixin` | `IEntityMobListener` |
| 自定义 GUI 事件 | `Handlers.Screen` | `IScreenListener` |
| 物品提示信息 | `Handlers.Tooltip` | `ITooltipListener` |

### A.2 注册与配方 (Registry & Recipes)

| 需求 | 使用的 API |
|---|---|
| 注册方块/物品 | `MinecraftRegistry` via `IGameRegistry.onGameRegistry()` |
| 注册自定义合成配方 | `Handlers.Crafting` + `CraftingRecipeRegisterEvent` |
| 创建自定义材料 | `GlacierMaterial(IEquipmentMaterial)` |

### A.3 网络通信 (Network)

| 需求 | 使用的 API |
|---|---|
| 发送网络包 | `Network.sendToClient/sendToAllPlayers/sendToServer` |
| 注册网络包读取器 | `PacketReader.registerServerPacketReader/registerClientPacketReader` |

### A.4 工具与辅助 (Utilities)

| 需求 | 使用的 API |
|---|---|
| 模组版本检测 | `FabricUtil.isModLoaded/compareModVersion` |
| 反射访问私有字段 | `Accessor.access/modify` |
| 加权随机 | `RandomUtil.getRandomEntry` |
| 获取游戏目录 | `FabricUtil.getGameDirectory/getConfigDirectory` |

---

## 附录 B：环境注解 (Environment)

RIC 使用 Legacy Fabric 的 `@Environment` 注解区分客户端/服务端：

```java
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)  // 仅客户端
@Environment(EnvType.SERVER)  // 仅服务端
```

部分 Handler 和 Listener 方法标注了运行环境，未标注的在双端均可调用。