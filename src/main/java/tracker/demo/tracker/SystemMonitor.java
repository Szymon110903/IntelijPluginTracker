package tracker.demo.tracker;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.GlobalMemory;

public class SystemMonitor {
    private final HardwareAbstractionLayer hal = new SystemInfo().getHardware();
    private final CentralProcessor processor = hal.getProcessor();
    private long[] prevTicks = new long[CentralProcessor.TickType.values().length];
    private long lastRecv = 0;
    private long lastRead = 0;

    public String getStats() {
        double cpu = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = processor.getSystemCpuLoadTicks();

        GlobalMemory memory = hal.getMemory();
        double totalGb = memory.getTotal() / 1024.0 / 1024.0 / 1024.0;
        double usedGb = totalGb - (memory.getAvailable() / 1024.0 / 1024.0 / 1024.0);
        int ramPercent = (int) ((usedGb / totalGb) * 100);

        NetworkIF net = hal.getNetworkIFs().get(0);
        long currentRecv = net.getBytesRecv();
        long netSpeed = (lastRecv == 0) ? 0 : (currentRecv - lastRecv) / 1024;
        lastRecv = currentRecv;

        long currentRead = hal.getDiskStores().get(0).getReadBytes();
        long diskSpeed = (lastRead == 0) ? 0 : (currentRead - lastRead) / (1024 * 1024);
        lastRead = currentRead;

        return String.format("%.1f|%d|%.1f/%.1f GB|%d|%d",
                cpu, ramPercent, usedGb, totalGb, netSpeed, diskSpeed);
    }

    public void forceGC() {
        System.gc();
    }

    public String getTopProcesses() {
        oshi.software.os.OperatingSystem os = new oshi.SystemInfo().getOperatingSystem();
        int logicalProcessorCount = processor.getLogicalProcessorCount();

        return os.getProcesses(null, java.util.Comparator.comparingDouble(oshi.software.os.OSProcess::getProcessCpuLoadCumulative).reversed(), 3).stream()
                .map(p -> String.format("%-15s %d%%",
                        p.getName().length() > 15 ? p.getName().substring(0, 12) + "..." : p.getName(),
                        (int)(100d * p.getProcessCpuLoadCumulative() / logicalProcessorCount)))
                .collect(java.util.stream.Collectors.joining("\n"));
    }
}