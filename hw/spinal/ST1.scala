import spinal.core._
import spinal.core.sim._
import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import scala.util.Using
import java.io.DataInputStream
import java.io.FileInputStream
import Util.readShorts

object Util {
    def spinalConfig(): SpinalConfig = SpinalConfig(
        targetDirectory = "hw/gen",
        onlyStdLogicVectorAtTopLevelIo = true
    )

    def readShorts(name: String): Array[Short] = 
        Using(new DataInputStream(new FileInputStream(name))) { din => 
            Iterator.continually(din.readShort).takeWhile(_ => din.available() > 0).toArray
        }.get
}

object ST1 extends App {
    val cfg = Util.spinalConfig
    cfg.generateVerilog(SubleqSOC(Some(readShorts("test.bin"))))
}

object ST1Test extends App {
    val cfg = Util.spinalConfig
    SimConfig
        .withWave
        .withConfig(cfg)
        .compile {
            val soc = SubleqSOC(None)
            
            soc.mem.simPublic()
            soc.sb_addr.simPublic()
            soc.sb_wdata.simPublic()
            soc.sb_write.simPublic()
            soc.io.uart_wdata.simPublic()
            soc.io.uart_rdata.simPublic()
            soc.io.uart_txe.simPublic()
            soc.io.uart_rxf.simPublic()
            soc.io.uart_wr.simPublic()
            soc.io.uart_rd.simPublic()
            
            soc
        }
        .doSim { soc =>
            soc.io.uart_rdata #= 0
            soc.io.uart_txe   #= false
            soc.io.uart_rxf   #= true

            val test_bin = Util.readShorts("test.bin")
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

            for(_ <- 0 until 8500) {
                clk.clockToggle()
                sleep(1)
                clk.clockToggle()
                sleep(1)

                //if(soc.sb_write.toBoolean && soc.sb_addr.toInt < 0) {
                //    print(soc.io.uart_wdata.toInt.toChar)
                //}

                if(soc.io.uart_wr.toBoolean) {
                    print(soc.io.uart_wdata.toInt.toChar)
                }
            }
        }
}