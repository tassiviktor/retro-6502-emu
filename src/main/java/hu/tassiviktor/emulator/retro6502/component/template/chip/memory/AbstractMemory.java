package hu.tassiviktor.emulator.retro6502.component.template.chip.memory;

import hu.tassiviktor.emulator.retro6502.common.Power2;
import hu.tassiviktor.emulator.retro6502.component.template.Component;
import hu.tassiviktor.emulator.retro6502.component.wireing.Bus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.logging.Logger;

public abstract class AbstractMemory implements Component {
    protected Logger logger;

    protected final long[] storage;
    protected final int addressLines;
    protected final int dataWidth;

    protected Bus addressBus;
    protected Bus dataBus;

    public AbstractMemory(int addressLines, int dataWidth) {
        logger = Logger.getLogger(this.getClass().getSimpleName());
        this.addressLines = addressLines;
        this.dataWidth = dataWidth;
        storage = new long[Power2.of(addressLines)];
        logger.info("Initializing "+this.getClass().getSimpleName()+" with size: "+storage.length);
    }

    /**
     * Ram chips generally contains random noise at start.
     */
    @Override
    public void powerUp(){

    }

    @Override
    public abstract void install();

    protected long read(int address){
        return storage[address] ;
    }

    protected void write(int address, long data){
        storage[address] = data;
    }

    public long[] dump() {
        return storage; //TODO copy
    }

    public void loadFromFile(String fileName) {
        //TODO expand to handle busWidth != 8
        try {
            byte[] data = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(fileName).toURI()));
            if(storage.length != data.length){
                logger.warning("Data and memory size differs. memory: "+storage.length+" file size: "+data.length);
            }
            for (int i = 0; i < data.length; i++) {
                storage[i] = Byte.toUnsignedLong(data[i]);
            }
            logger.info(fileName+" uploaded to memory");
        } catch (IOException  | URISyntaxException e) {
            throw new IllegalStateException("Cannot load memory data from"+fileName+". Error:"+e.getMessage());
        }
    }

    public void saveToFile(String fileName) {
        //TODO expand to handle busWidth != 8
    }

    public int getAddressLines() {
        return addressLines;
    }

    public int getDataWidth() {
        return dataWidth;
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
}
