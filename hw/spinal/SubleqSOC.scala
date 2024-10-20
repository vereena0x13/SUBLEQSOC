import scala.math

import spinal.core._
import nesoA7.UARTBus


case class SubleqSOC() extends Component {
    val io = new Bundle {
        val uart = new UARTBus()
    }

    
    io.uart.data_o := 0
    io.uart.wr := False
    io.uart.rd := False


    val cfg = SubleqConfig(
        width = 16
    )
    val subleq = Subleq(cfg)


    val ramSize = math.pow(2, cfg.width).toInt
    val mem = Mem(SInt(cfg.width bits), ramSize)


    val sb_addr = subleq.io.bus.addr
    val sb_wdata = subleq.io.bus.wdata
    val sb_write = subleq.io.bus.write


    when(sb_addr < 0) {
        subleq.io.bus.rdata := 0
    } otherwise {
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