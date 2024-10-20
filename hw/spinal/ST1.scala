import spinal.core._
import spinal.core.sim._
import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import java.io.DataInputStream
import java.io.FileInputStream

object ST1Constants {
    def spinalConfig(): SpinalConfig = SpinalConfig(
        targetDirectory = "hw/gen",
        defaultConfigForClockDomains = ClockDomainConfig(
            resetActiveLevel = LOW
        ),
        onlyStdLogicVectorAtTopLevelIo = true
    )
}

object ST1 extends App {
    val cfg = ST1Constants.spinalConfig
    cfg.generateVerilog(SubleqSOC())
}

object ST1Test extends App {
    def readShorts(name: String): Array[Short] = {
        val result = ArrayBuffer[Short]()

        val din = new DataInputStream(new FileInputStream(name))
        while(din.available() > 0) {
            result += din.readShort()
        }
        din.close()

        result.toArray
    }

    val cfg = ST1Constants.spinalConfig
    SimConfig
        .withWave
        .withConfig(cfg)
        .compile {
            val soc = SubleqSOC()
            
            soc.mem.simPublic()
            soc.sb_addr.simPublic()
            soc.sb_wdata.simPublic()
            soc.sb_write.simPublic()
            
            soc
        }
        .doSim { soc =>
            val test_bin = readShorts("test.bin")
            for(i <- 0 until test_bin.length) {
                val v = test_bin(i)
                val v2: Int = v & 0xFFFF
                soc.mem.setBigInt(i, BigInt(v2))
                sleep(0)
            }

            val clk = soc.clockDomain

            clk.fallingEdge()
            sleep(0)

            clk.assertReset()
            for(_ <- 0 until 10) {
                clk.clockToggle()
                sleep(1)
            }
            clk.deassertReset()
            sleep(1)

            for(_ <- 0 until 3000) {
                clk.clockToggle()
                sleep(1)
                clk.clockToggle()
                sleep(1)

                if(soc.sb_addr.toInt < 0 && soc.sb_write.toBoolean) {
                    print(soc.sb_wdata.toInt.toChar)
                }
            }
        }
}