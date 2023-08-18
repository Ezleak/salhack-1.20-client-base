package me.ionar.salhack.module.world;

import java.text.DecimalFormat;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerTick;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public final class TimerModule extends Module
{

    public final Value<Float> speed = new Value<Float>("Speed", new String[]
            { "Spd" }, "Tick-rate multiplier. [(20tps/second) * (this value)]", 4.0f, 0.1f, 10.0f, 0.1f);
    public final Value<Boolean> Accelerate = new Value<Boolean>("Accelerate", new String[]
            { "Acc" }, "Accelerate's from 1.0 until the anticheat lags you back", false);
    public final Value<Boolean> TPSSync = new Value<Boolean>("TPSSync", new String[]
            { "TPS" }, "Syncs the game time to the current TPS", false);

    private Timer timer = new Timer();

    public TimerModule()
    {
        super("Timer", new String[]
                { "Time", "Tmr" }, "Speeds up the client tick rate", 0, 0x24DBA3, ModuleType.WORLD);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        SalHack.TICK_TIMER = 1;
    }

    private float OverrideSpeed = 1.0f;

    /// store this as member to save cpu
    private DecimalFormat l_Format = new DecimalFormat("#.#");

    @Override
    public String getMetaData()
    {
        if (OverrideSpeed != 1.0f)
            return String.valueOf(OverrideSpeed);

        if (TPSSync.getValue())
        {
            float l_TPS = TickRateManager.Get().getTickRate();

            return l_Format.format((l_TPS/20));
        }

        return l_Format.format(GetSpeed());
    }

    @EventHandler
    private Listener<EventPlayerTick> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (OverrideSpeed != 1.0f && OverrideSpeed > 0.1f)
        {
            SalHack.TICK_TIMER = (int) (1 * OverrideSpeed);
            return;
        }

        if (TPSSync.getValue())
        {
            float l_TPS = TickRateManager.Get().getTickRate();

            SalHack.TICK_TIMER = (int) Math.min(0.1,(20/l_TPS));
        }
        else
            SalHack.TICK_TIMER = (int) (1 * GetSpeed());

        if (Accelerate.getValue() && timer.passed(2000))
        {
            timer.reset();
            speed.setValue(speed.getValue() + 0.1f);
        }
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof PlayerPositionLookS2CPacket && Accelerate.getValue())
        {
            speed.setValue(1.0f);
        }
    });

    private float GetSpeed()
    {
        return Math.max(speed.getValue(), 0.1f);
    }

    public void SetOverrideSpeed(float f)
    {
        OverrideSpeed = f;
    }

}
