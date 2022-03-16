package hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx;

import hu.tassiviktor.emulator.retro6502.component.wireing.Bus;
import hu.tassiviktor.emulator.retro6502.component.wireing.Line;

import static hu.tassiviktor.emulator.retro6502.common.GenericConstants.HIGH;
import static hu.tassiviktor.emulator.retro6502.common.GenericConstants.LOW;

/**
 * Connection layer
 */
public abstract class Cpu65xx extends Cpu65xxCore {

    protected Bus addressBus;
    protected Bus dataBus;

    protected Line nmi;
    protected Line irq;
    protected Line reset;
    protected Line rw;

    // Hard to do properly trigger pla to fetch when all buses are ready.
    // Fake phase shifted clock should do the trick
    protected Line clk2;

    @Override
    public void install() {
        super.install();
    }

    @Override
    public void powerUp() {
        super.powerUp();
    }

    public void run() {

        startCycleScheduler();
        startCycleLogThread();

        try {
            while (isRunning) {
                cycle();
                clk2.setState(LOW); //CLK2 is always set to low before next cycle.
            }
        } catch (Exception x) {
            Thread.currentThread().interrupt();
            x.printStackTrace();
        }
        System.out.println("Finis");
    }

    private void startCycleScheduler() {
        //TODO calibrate refresh period to achieve proper cycle speed
        int refresh = 477;
        Thread t = new Thread(() -> {
            //long cyclePerSec = 1_023_000;
            long cyclePerSec = 985_000;
            try {
                while (true) {
                    credit.set((int) (cyclePerSec / refresh));
                    cont.setSignal();
                    Thread.sleep(1000 / refresh, 1000 % refresh);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.start();
    }

    private void startCycleLogThread() {
        Thread t2 = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println(cycles.getAndSet(0L) / 4);
            }
        });
        t2.setDaemon(true);
        t2.start();
    }

    @Override
    protected byte readByteFromAddress(int address) {
        regAddress = address;
        addressBus.writeAsNumber(address);
        rw.setState(HIGH);
        //CLK2 tick to notify pla address is valid. More or less clk accurate way.
        clk2.setState(HIGH);
        return (byte) (dataBus.readAsNumber() & 0xFF);
    }

    @Override
    protected void writeByteToAddress(int address, int data) {
        regAddress = address;
        addressBus.writeAsNumber(address);
        dataBus.writeAsNumber(data);
        rw.setState(LOW);
        //CLK2 tick to notify pla address is valid. More or less clk accurate way.
        clk2.setState(HIGH);
    }

    @Override
    protected int readWordFromAddress(int address) {
        regAddress = address;
        int lo = readByteFromAddress(address);
        int hi = readByteFromAddress(address + 1);
        return hi * 256 + lo;
    }

    public Bus getAddressBus() {
        return addressBus;
    }

    public void setAddressBus(Bus addressBus) {
        this.addressBus = addressBus;
    }

    public Bus getDataBus() {
        return dataBus;
    }

    public void setDataBus(Bus dataBus) {
        this.dataBus = dataBus;
    }

    public Line getNmi() {
        return nmi;
    }

    public void setNmi(Line nmi) {
        this.nmi = nmi;
    }

    public Line getIrq() {
        return irq;
    }

    public void setIrq(Line irq) {
        this.irq = irq;
    }

    public Line getReset() {
        return reset;
    }

    public void setReset(Line reset) {
        this.reset = reset;
    }

    public Line getRw() {
        return rw;
    }

    public void setRw(Line rw) {
        this.rw = rw;
    }

    public Line getClk2() {
        return clk2;
    }

    public void setClk2(Line clk2) {
        this.clk2 = clk2;
    }
}
