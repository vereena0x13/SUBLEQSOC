import scala.collection.mutable.ArrayBuffer
import scala.math

import spinal.core._
import spinal.core.sim._

import Util._


case class SubleqSOC(initial_memory: Option[Array[Short]]) extends Component {
    val io = new Bundle {
        val uart_wdata = out(Bits(8 bits))
        val uart_rdata = in(Bits(8 bits))
	    val uart_txe   = in(Bool())
	    val uart_rxf   = in(Bool())
	    val uart_wr    = out(Bool())
	    val uart_rd    = out(Bool())
    }
    import io._


    val cfg = SubleqConfig(
        width = 16
    )
    val subleq = Subleq(cfg)

    val sb_addr  = subleq.io.bus.addr
    val sb_wdata = subleq.io.bus.wdata
    val sb_write = subleq.io.bus.write


    val ramSize = math.pow(2, cfg.width).toInt
    val mem     = Mem(SInt(cfg.width bits), ramSize)
    if(initial_memory.isDefined) {
        val arr = new ArrayBuffer[SInt]
        arr ++= initial_memory.get.map(x => S(x))
        while(arr.length < ramSize) arr += S(0)
        mem.init(arr)
    }


    val b_uart_wdata = Reg(Bits(8 bits)) init(0)
    val b_uart_wr    = Reg(Bool()) init(False)
    //val b_uart_rd    = Reg(Bool()) init(False)

    uart_wdata  := b_uart_wdata
    uart_wr     := b_uart_wr 
    //uart_rd     := b_uart_rd
    uart_rd     := False


    when(sb_addr < 0) {
        when(sb_write) {
            b_uart_wdata := sb_wdata(7 downto 0).asBits
            subleq.io.bus.rdata := 0

            when(!uart_txe & !b_uart_wr) {
                subleq.io.bus.ready := True
                b_uart_wr := True
            } elsewhen(b_uart_wr) {
                subleq.io.bus.ready := True
                b_uart_wr := False
            } otherwise {
                subleq.io.bus.ready := False
            }
        } otherwise {
            // TODO
            subleq.io.bus.ready := True
            subleq.io.bus.rdata := 0
        }     
    } otherwise {
        subleq.io.bus.ready := True

        mem.write(
            address = sb_addr.asUInt,
            data    = sb_wdata,
            enable  = sb_write
        )

        subleq.io.bus.rdata := mem.readAsync(
            address = sb_addr.asUInt
        )
    }
}


object GenerateSOC extends App {
    spinalConfig.generateVerilog(SubleqSOC(Some(readShorts("test.bin"))))
}


object TestSOC extends App {
    SimConfig
        .withWave
        .withConfig(spinalConfig)
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
            soc.b_uart_wdata.simPublic()
            
            soc
        }
        .doSim { soc =>
            soc.io.uart_rdata #= 0
            soc.io.uart_txe   #= false
            soc.io.uart_rxf   #= true

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

            for(_ <- 0 until 8500) {
                clk.clockToggle()
                sleep(1)
                clk.clockToggle()
                sleep(1)

                //if(soc.sb_write.toBoolean && soc.sb_addr.toInt < 0) {
                //    print(soc.io.uart_wdata.toInt.toChar)
                //}

                if(soc.io.uart_wr.toBoolean) {
                    print(soc.b_uart_wdata.toInt.toChar)
                }
            }
        }
}