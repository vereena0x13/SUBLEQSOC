package nesoA7

import spinal.core._


case class UARTBus() extends Bundle {
    val DATA_WIDTH: Int = 8
    val data_i = in(Bits(DATA_WIDTH bits))
    val data_o = out(Bits(DATA_WIDTH bits))
    val txe    = in(Bool())
    val rxf    = in(Bool())
    val wr     = out(Bool())
    val rd     = out(Bool())
}


case class UARTController() extends Component {
    val io = new Bundle {
        val bus = new UARTBus()
        
    }
}