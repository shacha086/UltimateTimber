package com.songoda.ultimatetimber.tree;

import org.bukkit.block.Block;

public class DetectedTree {

    public enum Directions {
        VERTICAL, HORIZONTAL
    }

    private final TreeDefinition treeDefinition;
    private final TreeBlockSet<Block> detectedTreeBlocks;
    private final Directions direction;

    public DetectedTree(TreeDefinition treeDefinition, TreeBlockSet<Block> detectedTreeBlocks, Directions direction) {
        this.treeDefinition = treeDefinition;
        this.detectedTreeBlocks = detectedTreeBlocks;
        this.direction = direction;
    }

    /**
     * Gets the TreeDefinition of this detected tree
     *
     * @return The TreeDefinition of this detected tree
     */
    public TreeDefinition getTreeDefinition() {
        return this.treeDefinition;
    }

    /**
     * Gets the blocks that were detected as part of this tree
     *
     * @return A TreeBlockSet of detected Blocks
     */
    public TreeBlockSet<Block> getDetectedTreeBlocks() {
        return this.detectedTreeBlocks;
    }

    /**
     * Gets the tree arrangement direction
     *
     * @return A Directions enum
     */
    public Directions getDirection() {
        return this.direction;
    }
}
