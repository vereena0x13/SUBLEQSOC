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


    uart_wdata.setAsReg() init(0)
    uart_wr.setAsReg() init(False)
    uart_rd := False


    val cfg = SubleqConfig(
        width = 16
    )
    val subleq = Subleq(cfg)
    import subleq.io._


    val ramSize = math.pow(2, cfg.width).toInt
    val mem = Mem(UInt(cfg.width bits), ramSize)
    if(initial_memory.isDefined) {
        assert(initial_memory.get.length <= ramSize)
        val arr = new ArrayBuffer[BigInt]
        arr ++= initial_memory.get.map(x => BigInt(x))
        while(arr.length < ramSize) arr += BigInt(0)
        mem.initBigInt(arr, true)
    }

    
    bus.cmd.ready := False
    bus.rsp.data := 0


    val is_putc = bus.cmd.addr === 65535
    val is_getc = bus.cmd.addr === 65534
    val is_mmio = is_putc | is_getc

    val rd = mem.readWriteSync(
        address = bus.cmd.addr,
        data    = bus.cmd.data,
        enable  = bus.cmd.valid & !is_mmio & !clockDomain.readResetWire,
        write   = bus.cmd.write
    )

    val delayed_cmd_valid = RegNext(bus.cmd.valid)
    when(bus.cmd.valid & !clockDomain.readResetWire) {
        when(is_mmio) {
            when(bus.cmd.write) {
                when(!uart_txe) {
                    uart_wdata := bus.cmd.data(7 downto 0).asBits
                    uart_wr := True

                    when(delayed_cmd_valid) {
                        bus.cmd.ready := True
                    }
                }
            } otherwise {
                when(delayed_cmd_valid) {
                    bus.cmd.ready := True
                }
            }
        } otherwise {
            when(!bus.cmd.write) {
                bus.rsp.data := rd
            }
            when(delayed_cmd_valid) {
                bus.cmd.ready := True
            }
        }
    }
}


object GenerateSOC extends App {
    spinalConfig.generateVerilog(SubleqSOC(Some(readShorts("test.bin"))))
}


object SimulateSOC extends App {
    SimConfig
        .withFstWave
        .withConfig(spinalConfig)
        .compile {
            val soc = SubleqSOC(None)
            
            soc.mem.simPublic()
            soc.io.simPublic()
            soc.io.uart_wdata.simPublic()
            soc.io.uart_rdata.simPublic()
            soc.io.uart_txe.simPublic()
            soc.io.uart_rxf.simPublic()
            soc.io.uart_wr.simPublic()
            soc.io.uart_rd.simPublic()
            soc.is_mmio.simPublic()
            soc.subleq.io.bus.cmd.valid.simPublic()
            soc.subleq.io.bus.cmd.ready.simPublic()
            soc.subleq.io.bus.cmd.write.simPublic()
            soc.subleq.io.bus.cmd.addr.simPublic()
            soc.subleq.io.bus.cmd.data.simPublic()

            soc
        }
        .doSim { soc =>
            soc.io.uart_rdata #= 0
            soc.io.uart_txe   #= false
            soc.io.uart_rxf   #= true

            val test_bin = readShorts("subleq_codegen/test.bin")
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

            for(_ <- 0 until 5000) {
                clk.clockToggle()
                sleep(1)
                clk.clockToggle()
                sleep(1)

                //if(soc.bus.write.toBoolean && soc.bus.addr.toInt < 0) {
                //    print(soc.io.uart_wdata.toInt.toChar)
                //}

                if(soc.subleq.io.bus.cmd.valid.toBoolean &&
                   soc.subleq.io.bus.cmd.ready.toBoolean &&
                   soc.subleq.io.bus.cmd.write.toBoolean &&
                   soc.is_mmio.toBoolean) {
                    print(soc.io.uart_wdata.toInt.toChar)
                }
            }
        }
}