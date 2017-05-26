//
//  AssetCell.m
//
//  Created by ELC on 2/15/11.
//  Copyright 2011 ELC Technologies. All rights reserved.
//

#import "ELCAssetCell.h"
#import "ELCAsset.h"

@interface ELCAssetCell ()

@property (nonatomic, strong) NSArray *rowAssets;
@property (nonatomic, strong) NSMutableArray *imageViewArray;
@property (nonatomic, strong) NSMutableArray *selectedViewArray;

@end

@implementation ELCAssetCell

//Using auto synthesizers

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseIdentifier];
	if (self) {
        UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(cellTapped:)];
        [self addGestureRecognizer:tapRecognizer];
        
        NSMutableArray *mutableArray = [[NSMutableArray alloc] initWithCapacity:4];
        self.imageViewArray = mutableArray;
        
        NSMutableArray *selectedViewArray = [[NSMutableArray alloc] initWithCapacity:4];
        self.selectedViewArray = selectedViewArray;
	}
	return self;
}

- (void)setAssets:(NSArray *)assets
{
    self.rowAssets = assets;
	for (UIImageView *view in _imageViewArray) {
        [view removeFromSuperview];
	}

    for (UILabel *view in _selectedViewArray) {
        [view removeFromSuperview];
    }

    UIFont *donkeyFont = [UIFont fontWithName:@"DonkeyFont" size:25];
    UILabel *overlayLabel = nil;
    CGSize overlaySize;
    for (int i = 0; i < [_rowAssets count]; ++i) {

        ELCAsset *asset = [_rowAssets objectAtIndex:i];

        if (i < [_imageViewArray count]) {
            UIImageView *imageView = [_imageViewArray objectAtIndex:i];
            imageView.image = [UIImage imageWithCGImage:asset.asset.thumbnail];
            overlaySize = CGSizeMake(CGRectGetWidth(imageView.frame), CGRectGetHeight(imageView.frame));
        } else {
            UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageWithCGImage:asset.asset.thumbnail]];
            [_imageViewArray addObject:imageView];
            overlaySize = CGSizeMake(CGRectGetWidth(imageView.frame), CGRectGetHeight(imageView.frame));
        }

        if (i < [_selectedViewArray count]) {
            UILabel *overlayLabel = [_selectedViewArray objectAtIndex:i];
            [self selectLabel:overlayLabel selected:asset.selected];
        } else {
            overlayLabel = [[UILabel alloc] init];
            overlayLabel.textColor = [UIColor whiteColor];
            overlayLabel.font = donkeyFont;
            overlayLabel.textAlignment = NSTextAlignmentLeft;
            [_selectedViewArray addObject:overlayLabel];
            [self selectLabel:overlayLabel selected:asset.selected];
        }
    }
}

- (void)cellTapped:(UITapGestureRecognizer *)tapRecognizer
{
    CGPoint point = [tapRecognizer locationInView:self];
    CGFloat totalWidth = self.rowAssets.count * 75 + (self.rowAssets.count - 1) * 4;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;
    
	CGRect frame = CGRectMake(startX, 2, 75, 75);
	
    for (int i = 0; i < [_rowAssets count]; ++i) {

        if (CGRectContainsPoint(frame, point)) {
            ELCAsset *asset = [_rowAssets objectAtIndex:i];
            asset.selected = !asset.selected;
            UILabel *overlayLabel = [_selectedViewArray objectAtIndex:i];
            [self selectLabel:overlayLabel selected:asset.selected];
            break;
        }
        frame.origin.x = frame.origin.x + frame.size.width + 4;
    }
}

- (void)layoutSubviews
{    
    CGFloat totalWidth = self.rowAssets.count * 75 + (self.rowAssets.count - 1) * 4;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;
    
	CGRect frame = CGRectMake(startX, 2, 75, 75);
	
	for (int i = 0; i < [_rowAssets count]; ++i) {
		UIImageView *imageView = [_imageViewArray objectAtIndex:i];
		[imageView setFrame:frame];
		[self addSubview:imageView];
        
        UILabel *overlayLabel = [_selectedViewArray objectAtIndex:i];
        CGRect overlayFrame = CGRectMake(frame.origin.x + 5,
                                         frame.origin.y + 5,
                                         frame.size.width,
                                         frame.size.height / 3);
        [overlayLabel setFrame:overlayFrame];
        [self addSubview:overlayLabel];
		
		frame.origin.x = frame.origin.x + frame.size.width + 4;
	}
}

- (void)selectLabel:(UILabel*)label selected:(bool)selected {
    label.text = selected ? @"\uE060" : @"\uE061";
    label.textColor = selected ? [UIColor colorWithRed:22.0f / 255.0f
                                                 green:157 / 255.0f
                                                  blue:217.0f / 255.0f
                                                 alpha:1.0] : [UIColor whiteColor];
}


@end
